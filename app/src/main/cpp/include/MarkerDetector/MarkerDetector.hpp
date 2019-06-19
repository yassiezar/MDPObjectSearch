#include <whycon/imageproc/CCircleDetect.h>
#include <whycon/imageproc/CTransformation.h>
#include <whycon/common/CRawImage.h>

#include <cstring>

#include <android/log.h>

#define MAX_IDS 1
#define MARKERLOG "MarkerDetector"

namespace MarkerDetector
{
    class MarkerDetector
    {
    public:
        MarkerDetector(int, int);
        ~MarkerDetector();

        bool init(float *focalLen, float *principalPoint, float *distortionMatrix);
        bool kill();

        void processImage(unsigned char* data);
        const int getImageWidth() { return imageWidth; }
        const int getImageHeight() { return imageHeight; }

    private:
        CCircleDetect* patternDetectors[MAX_IDS];
        SSegment currentSegmentArray[MAX_IDS];
        SSegment lastSegmentArray[MAX_IDS];
        STrackedObject objectArray[MAX_IDS];

        CTransformation* trans;
        CRawImage* image;

        int imageWidth, imageHeight;

        const float circleDiameter = 0.122f;                 // Adjust the outer marker width [m]
    };
}