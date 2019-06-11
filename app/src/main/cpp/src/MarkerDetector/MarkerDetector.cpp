#include <MarkerDetector/MarkerDetector.hpp>

namespace MarkerDetector
{
    MarkerDetector::MarkerDetector(int imageWidth, int imageHeight) : imageWidth(imageWidth), imageHeight(imageHeight) { }

    bool MarkerDetector::init()
    {
        trans = new CTransformation(imageWidth, imageHeight, circleDiameter, true);
        trans->transformType = TRANSFORM_NONE;

        image = new CRawImage(imageWidth, imageHeight);

        for(int i = 0; i < MAX_IDS; i ++)
        {
            patternDetectors[i] = new CCircleDetect(imageWidth, imageHeight, i);
        }

        return 0;
    }

    bool MarkerDetector::kill()
    {
        delete image;
        delete trans;

        for(int i = 0; i < MAX_IDS; i ++)
        {
            delete patternDetectors[i];
        }

        return 0;
    }

    void MarkerDetector::processImage(unsigned char *data)
    {
        // Copy in new image data
        memcpy(image->data, data, sizeof(image->data));

        // Track markers found in previous search
        for(int i = 0; i < MAX_IDS; i ++)
        {
            if(currentSegmentArray[i].valid)
            {
                lastSegmentArray[i] = currentSegmentArray[i];
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
            }
        }

        // Find untracked markers
        for(int i = 0; i < MAX_IDS; i++)
        {
            if(!currentSegmentArray[i].valid)
            {
                lastSegmentArray[i].valid = false;
                currentSegmentArray[i] = patternDetectors[i]->findSegment(image, lastSegmentArray[i]);
            }
            if(!currentSegmentArray[i].valid) break;
        }

        // Perform coordinate transformation
        for(int i = 0; i < MAX_IDS; i++)
        {
            if(currentSegmentArray[i].valid)
            {
                if(currentSegmentArray[i].valid)
                {
                    objectArray[i] = trans->transform(currentSegmentArray[i], false);
                }
            }
        }
    }
}