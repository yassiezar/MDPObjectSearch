#include <whycon/imageproc/CCircleDetect.h>
#include <whycon/imageproc/CTransformation.h>
#include <whycon/common/CRawImage.h>

#include <cstring>
#include <cmath>

#include <android/log.h>
#include <jni.h>

#define MAX_IDS 8
#define MARKERLOG "MarkerDetector"
#define NOTHING 0

namespace MarkerDetector
{
    class MarkerDetector
    {
    public:
        MarkerDetector(int, int);
        ~MarkerDetector();

        bool init(float *focalLen, float *principalPoint, float *distortionMatrix);
        bool kill();

        std::vector<STrackedObject> processImage(unsigned char *data);
        const int getImageWidth() { return imageWidth; }
        const int getImageHeight() { return imageHeight; }

    private:
        CCircleDetect* patternDetectors[MAX_IDS];
         SSegment currentSegmentArray[MAX_IDS];
         SSegment lastSegmentArray[MAX_IDS];

        CTransformation* trans;
        CRawImage* image;

        int imageWidth, imageHeight;

        const float circleDiameter = 0.165f;                 // Adjust the outer marker width [m]
    };
}