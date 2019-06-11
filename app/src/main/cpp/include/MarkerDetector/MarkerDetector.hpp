#include <whycon/imageproc/CCircleDetect.h>
#include <whycon/imageproc/CTransformation.h>
#include <whycon/common/CRawImage.h>

#include <cstring>

#define MAX_IDS 16

namespace MarkerDetector
{
    class MarkerDetector
    {
    public:
        MarkerDetector(int, int);

        bool init();
        bool kill();

        void processImage(unsigned char* data);

    private:
        CCircleDetect* patternDetectors[MAX_IDS];
        SSegment currentSegmentArray[MAX_IDS];
        SSegment lastSegmentArray[MAX_IDS];
        STrackedObject objectArray[MAX_IDS];

        CTransformation* trans;
        CRawImage* image;

        int imageWidth, imageHeight;

        const float circleDiameter = 0.122;                 // Adjust the outer marker width [m]
    };
}