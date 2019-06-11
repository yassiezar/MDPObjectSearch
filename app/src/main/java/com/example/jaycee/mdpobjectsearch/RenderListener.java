package com.example.jaycee.mdpobjectsearch;

import com.google.ar.core.Frame;

public interface RenderListener
{
    void onViewportChange(int width, int height);
    void onDrawRequest(int textureId);
    Frame onFrameRequest();
}
