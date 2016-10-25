/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.Books.app.Books;

public class Shaders 
{
	
	public static final String cubeMeshVertexShader =
	"attribute vec4 vertexPosition; " +
	"attribute vec2 vertexTexCoord; " +
	" " +
	"varying vec2 texCoord; " +
	" " +
	"uniform mat4 modelViewProjectionMatrix; " +
	" " +
	"void main() " +
	"{ " +
	"   gl_Position = modelViewProjectionMatrix * vertexPosition; " +
	"   texCoord = vertexTexCoord; " +
	"} ";

	public static final String cubeFragmentShader =
	"precision mediump float; " +
	" " +
	"varying vec2 texCoord; " +
	" " +
	"uniform sampler2D texSampler2D; " +
	" " +
	"void main() " +
	"{ " +
	"   gl_FragColor = texture2D(texSampler2D, texCoord); " +
	"} ";
}
