#include <MarkerDetector/MarkerDetector.hpp>

namespace MarkerDetector
{
    MarkerDetector::MarkerDetector(int imageWidth, int imageHeight) : imageWidth(imageWidth), imageHeight(imageHeight) { }

    bool MarkerDetector::init()
    {
        trans = new CTransformation(imageWidth, imageHeight, circleDiameter, true);
        return 0;
    }
}