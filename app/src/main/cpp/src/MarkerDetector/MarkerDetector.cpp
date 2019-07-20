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

        return true;
    }

    MarkerDetector::~MarkerDetector()
    {
        // kill();
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

    std::vector<STrackedObject> MarkerDetector::processImage(unsigned char *data)
    {
        // Copy in new image data
        image->data = data;

        std::vector<STrackedObject> markers;
        // Track markers found in previous search
        for(int i = 0; i < MAX_IDS; i ++)
        {
            if(currentSegmentArray[i].valid)
            {
//                 __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Updating tracked markers");
                lastSegmentArray[i] = currentSegmentArray[i];
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
//                 __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "previous track: Segment %d coords: %f, %f", currentSegmentArray[i].ID, currentSegmentArray[i].x, currentSegmentArray[i].y);
            }
        }

        // Find untracked markers
        for(int i = 0; i < MAX_IDS; i++)
        {
            if(!currentSegmentArray[i].valid)
            {
//                 __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Searching for untracked markers");
                lastSegmentArray[i].valid = false;
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
//                 __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "new track: Segment %d coords: %f, %f", currentSegmentArray[i].ID, currentSegmentArray[i].x, currentSegmentArray[i].y);
                // __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Valid: %d", currentSegmentArray[i].valid);
            }
            if(!currentSegmentArray[i].valid) break;
        }

        // Perform coordinate transformation
//        for(int i = 0; i < MAX_IDS; i++)
        for(auto segment : currentSegmentArray)
        {
            if(segment.valid)
            {
                markers.push_back(trans->transform(segment));
//                 __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Transforming tracked markers");
//                objectArray[i] = trans->transform(currentSegmentArray[i]);
//                __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "ID: %d angle: %f, %f, %f norm: %f", objectArray[i].ID, objectArray[i].roll, objectArray[i].pitch, objectArray[i].yaw, sqrt(objectArray[i].roll*objectArray[i].roll + objectArray[i].pitch*objectArray[i].pitch + objectArray[i].yaw*objectArray[i].yaw));
            }
/*            else
            {
                 objectArray[i].ID = NOTHING;
            }*/
        }

/*        if(currentSegmentArray[0].valid)
        {
        }*/
        // __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "new track: Segment %d coords: %f, %f, %f, %f angle: %f, %f, %f", objectArray[0].ID, objectArray[0].x, objectArray[0].y, objectArray[0].z, objectArray[0].d, objectArray[0].roll, objectArray[0].pitch, objectArray[0].yaw);
        return markers;
        //return NOTHING;
    }
}