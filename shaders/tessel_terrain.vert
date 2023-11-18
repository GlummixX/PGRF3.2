#version 460 core

layout (location = 0) in vec2 inPosition;

out vec2 uvs;

void main()
{
    gl_Position = vec4(inPosition.xy, 1.0);
    uvs = vec2(0.);
}