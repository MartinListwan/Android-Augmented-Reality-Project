/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.Books.app.Books;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.vuforia.Matrix34F;
import com.vuforia.Matrix44F;
import com.vuforia.Tool;
import com.vuforia.Vec4F;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;


public class Transition3Dto2D
{
    private boolean isActivityPortraitMode;
    private int screenWidth;
    private int screenHeight;
    private Vec4F screenRect;
    private float[] identityMatrix = new float[16];
    private float[] orthoMatrix = new float[16];
    
    private int shaderProgramID;
    private int normalHandle;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    
    private float animationLength;
    private int animationDirection;
    private float dpiScaleIndicator;
    private long animationStartTime;
    private boolean animationFinished;
    private float scaleFactor;
    private Plane mPlane;
    
    
    public Transition3Dto2D(int screenWidth, int screenHeight,
        boolean isPortraitMode, float dpiSIndicator, float scaleFactor, Plane p)
    {
        identityMatrix[0] = 1.0f;
        identityMatrix[1] = 0.0f;
        identityMatrix[2] = 0.0f;
        identityMatrix[3] = 0.0f;
        identityMatrix[4] = 0.0f;
        identityMatrix[5] = 1.0f;
        identityMatrix[6] = 0.0f;
        identityMatrix[7] = 0.0f;
        identityMatrix[8] = 0.0f;
        identityMatrix[9] = 0.0f;
        identityMatrix[10] = 1.0f;
        identityMatrix[11] = 0.0f;
        identityMatrix[12] = 0.0f;
        identityMatrix[13] = 0.0f;
        identityMatrix[14] = 0.0f;
        identityMatrix[15] = 1.0f;
        
        this.dpiScaleIndicator = dpiSIndicator;
        this.scaleFactor = scaleFactor;
        updateScreenPoperties(screenWidth, screenHeight, isPortraitMode);
        mPlane = p;
    }
    
    
    public void initializeGL(int sProgramID)
    {
        shaderProgramID = sProgramID;
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        
        SampleUtils.checkGLError("Transition3Dto2D.initializeGL");
    }
    
    
    public void updateScreenPoperties(int screenWidth, int screenHeight,
        boolean isPortraitMode)
    {
        this.isActivityPortraitMode = isPortraitMode;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        screenRect = new Vec4F(0, 0, screenWidth, screenHeight);
        
        for (int i = 0; i < 16; i++)
            orthoMatrix[i] = 0.0f;
        float nLeft = -screenWidth / 2.0f;
        float nRight = screenWidth / 2.0f;
        float nBottom = -screenHeight / 2.0f;
        float nTop = screenHeight / 2.0f;
        float nNear = -1.0f;
        float nFar = 1.0f;
        
        orthoMatrix[0] = 2.0f / (nRight - nLeft);
        orthoMatrix[5] = 2.0f / (nTop - nBottom);
        orthoMatrix[10] = 2.0f / (nNear - nFar);
        orthoMatrix[12] = -(nRight + nLeft) / (nRight - nLeft);
        orthoMatrix[13] = -(nTop + nBottom) / (nTop - nBottom);
        orthoMatrix[14] = (nFar + nNear) / (nFar - nNear);
        orthoMatrix[15] = 1.0f;
        
    }
    
    
    public void setScreenRect(int centerX, int centerY, int width, int height)
    {
        screenRect = new Vec4F(centerX, centerY, width, height);
    }
    
    
    public void startTransition(float duration, boolean inReverse,
        boolean keepRendering)
    {
        animationLength = duration;
        animationDirection = inReverse ? -1 : 1;
        
        animationStartTime = getCurrentTimeMS();
        animationFinished = false;
    }
    
    
    public float stepTransition()
    {
        float timeElapsed = (getCurrentTimeMS() - animationStartTime) / 1000.0f;
        
        float t = timeElapsed / animationLength;
        if (t >= 1.0f)
        {
            t = 1.0f;
            animationFinished = true;
        }
        
        if (animationDirection == -1)
        {
            t = 1.0f - t;
        }
        
        return t;
    }
    
    
    public void render(float[] mProjectionMatrix, Matrix34F targetPose,
        int texture1)
    {
        float t = stepTransition();
        
        float[] modelViewProjectionTracked = new float[16];
        float[] modelViewProjectionCurrent = new float[16];
        Matrix44F modelViewMatrixVuforia = Tool
            .convertPose2GLMatrix(targetPose);
        float[] modelViewMatrix = modelViewMatrixVuforia.getData();
        float[] finalPositionMatrix = getFinalPositionMatrix();
        
        Matrix.scaleM(modelViewMatrix, 0, 430.0f * scaleFactor,
            430.0f * scaleFactor, 1.0f);
        Matrix.multiplyMM(modelViewProjectionTracked, 0, mProjectionMatrix, 0,
            modelViewMatrix, 0);
        
        float elapsedTransformationCurrent = 0.8f + 0.2f * t;
        elapsedTransformationCurrent = deccelerate(elapsedTransformationCurrent);
        linearInterpolate(modelViewProjectionTracked, finalPositionMatrix,
            modelViewProjectionCurrent, elapsedTransformationCurrent);
        
        GLES20.glUseProgram(shaderProgramID);
        
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false,
            0, mPlane.getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false,
            0, mPlane.getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT,
            false, 0, mPlane.getTexCoords());
        
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        GLES20.glEnable(GLES20.GL_BLEND);
        
        // Drawing Textured Plane
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
            modelViewProjectionCurrent, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT,
            mPlane.getIndices());
        
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
        
        SampleUtils.checkGLError("Transition3Dto2D.render");
        
    }
    
    
    public boolean transitionFinished()
    {
        return animationFinished;
    }
    
    
    public Matrix34F getFinalPositionMatrix34F()
    {
        float[] glFinalPositionMatrix = getFinalPositionMatrix();
        float[] vuforiaFinalPositionMatrix = new float[12];
        
        for(int i = 0; i < 4; i ++)
        {
            for( int j = 0; j < 3; j++)
            {
                vuforiaFinalPositionMatrix[i*3+j] = glFinalPositionMatrix[i*4+j];
            }
        }
        
        Matrix34F finalPositionMatrix34F = new Matrix34F();
        finalPositionMatrix34F.setData(vuforiaFinalPositionMatrix);
        return finalPositionMatrix34F;
    }
    
    private float[] getFinalPositionMatrix()
    {
        int tempValue;
        float[] viewport = new float[4];
        GLES20.glGetFloatv(GLES20.GL_VIEWPORT, viewport, 0);
        
        // Sometimes the screenWidth and screenHeight values
        // are not properly updated, so this workaround
        // ensures that it will work fine every time
        if (isActivityPortraitMode)
        {
            if (screenWidth > screenHeight)
            {
                tempValue = screenWidth;
                screenWidth = screenHeight;
                screenHeight = tempValue;
            }
        } else
        {
            if (screenWidth < screenHeight)
            {
                tempValue = screenWidth;
                screenWidth = screenHeight;
                screenHeight = tempValue;
            }
        }
        
        float scaleFactorX = screenWidth / viewport[2];
        float scaleFactorY = screenHeight / viewport[3];
        
        // General Multiplier for getting the final size of the
        // plane when the animation is finished
        float scaleMultiplierWidth = 1.3f;
        float scaleMultiplierHeight = 0.75f;
        
        // Multiplies the value depending on different screen
        // dpis to get the same final size across all devices
        
        // MDPI devices
        if (dpiScaleIndicator == 1)
        {
            scaleMultiplierHeight *= 1.6f;
            scaleMultiplierWidth *= 1.6f;
        }
        // HDPI devices
        else if (dpiScaleIndicator == 1.5f)
        {
            scaleMultiplierHeight *= 1.2f;
            scaleMultiplierWidth *= 1.2f;
        }
        
        // XHDPI devices
        else if (dpiScaleIndicator == 2.0f)
        {
            scaleMultiplierHeight *= 0.9f;
            scaleMultiplierWidth *= 0.9f;
        }
        
        // XXHDPI devices
        else if (dpiScaleIndicator > 2.0f)
        {
            scaleMultiplierHeight *= 0.75f;
            scaleMultiplierWidth *= 0.75f;
        }
        
        float translateX = screenRect.getData()[0] * scaleFactorX;
        float translateY = screenRect.getData()[1] * scaleFactorY;
        
        float scaleX = screenRect.getData()[2] * scaleFactorX;
        float scaleY = screenRect.getData()[3] * scaleFactorY;
        
        float[] result = orthoMatrix.clone();
        Matrix.translateM(result, 0, translateX, translateY, 0.0f);
        
        if (isActivityPortraitMode)
            Matrix.scaleM(result, 0, scaleX * scaleMultiplierWidth, scaleY
                * scaleMultiplierHeight, 1.0f);
        else
            Matrix.scaleM(result, 0, scaleX * scaleMultiplierHeight, scaleY
                * scaleMultiplierWidth, 1.0f);
        
        return result;
    }
    
    
    private float deccelerate(float val)
    {
        return (1 - ((1 - val) * (1 - val)));
    }
    
    
    private void linearInterpolate(float[] start, float[] end, float[] current,
        float elapsed)
    {
        if (start.length != 16 || end.length != 16 || current.length != 16)
            return;
        
        // ATTENTION: This is a plain matrix linear interpolation. It isn't
        // elegant but it gets the job done.
        // A better approach would be to interpolate the modelview and
        // projection matrices separately and to use some
        // sort of curve such as bezier
        for (int i = 0; i < 16; i++)
            current[i] = ((end[i] - start[i]) * elapsed) + start[i];
    }
    
    
    private long getCurrentTimeMS()
    {
        return System.currentTimeMillis();
    }
    
}
