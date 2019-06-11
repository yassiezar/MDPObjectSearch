#include <whycon/imageproc/CCircleDetect.h>
#include <whycon/imageproc/CTransformation.h>
#include <whycon/common/CRawImage.h>

#define MAX_IDS 16

namespace MarkerDetector
{
    class MarkerDetector
    {
    public:
        MarkerDetector(int, int);

        bool init();
        bool kill();

    private:
        CCircleDetect* patternDetectors[MAX_IDS];
        CTransformation* trans;
        CRawImage* image;

        int imageWidth, imageHeight;

        const float circleDiameter = 0.122;                 // Adjust the outer marker width [m]
    };
}