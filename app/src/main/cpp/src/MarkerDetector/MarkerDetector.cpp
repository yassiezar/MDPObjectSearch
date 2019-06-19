#include <MarkerDetector/MarkerDetector.hpp>

namespace MarkerDetector
{
    MarkerDetector::MarkerDetector(int imageWidth, int imageHeight) :
            imageWidth(imageWidth), imageHeight(imageHeight)
    { }

    bool MarkerDetector::init(float *focalLen, float *principalPoint, float *distortionMatrix)
    {
        image = new CRawImage(imageWidth, imageHeight);
        image->ownData = false;
        image->bpp = 4;

        float zeroErr[2] = {0.F, 0.F};
        float zeroDistErr[6] = {0.F, 0.F, 0.F, 0.F, 0.F, 0.F};
        trans = new CTransformation(imageWidth, imageHeight, circleDiameter, true);
        trans->transformType = TRANSFORM_NONE;
        trans->setCameraParams(focalLen, principalPoint, distortionMatrix, zeroErr, zeroDistErr);

        for(int i = 0; i < MAX_IDS; i ++)
        {
            patternDetectors[i] = new CCircleDetect(imageWidth, imageHeight, i);
            patternDetectors[i]->draw = true;
        }

        __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Initialised detectors.");

        return 0;
    }

    MarkerDetector::~MarkerDetector()
    {
        kill();
    }

    bool MarkerDetector::kill()
    {
        delete image;
        delete trans;

        for(int i = 0; i < MAX_IDS; i ++)
        {
            delete patternDetectors[i];
        }

        __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Killed detectors.");

        return 0;
    }

    void MarkerDetector::processImage(unsigned char *data)
    {
        // Copy in new image data
        image->data = data;

        // Track markers found in previous search
        for(int i = 0; i < MAX_IDS; i ++)
        {
            if(currentSegmentArray[i].valid)
            {
                lastSegmentArray[i] = currentSegmentArray[i];
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
                __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "previous track: Segment %d coords: %f, %f", currentSegmentArray[i].ID, currentSegmentArray[i].x, currentSegmentArray[i].y);
            }
        }

        // Find untracked markers
        for(int i = 0; i < MAX_IDS; i++)
        {
            if(!currentSegmentArray[i].valid)
            {
                lastSegmentArray[i].valid = false;
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
                __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "new track: Segment %d coords: %f, %f", currentSegmentArray[i].ID, currentSegmentArray[i].x, currentSegmentArray[i].y);
                __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Valid: %d", currentSegmentArray[i].valid);
            }
            if(!currentSegmentArray[i].valid) break;
        }

        // Perform coordinate transformation
        for(int i = 0; i < MAX_IDS; i++)
        {
            if(currentSegmentArray[i].valid)
            {
                objectArray[i] = trans->transform(currentSegmentArray[i], false);
                __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "new track: Segment %d coords: %f, %f, %f, %f angle: %f, %f, %f", objectArray[i].ID, objectArray[i].x, objectArray[i].y, objectArray[i].z, objectArray[i].d, objectArray[i].roll, objectArray[i].pitch, objectArray[i].yaw);
            }
        }
    }
}