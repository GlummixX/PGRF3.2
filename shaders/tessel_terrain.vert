#version 460 core

layout (location = 0) in vec2 inPosition;

void main()
{
    vec4 position = vec4(inPosition.x, inPosition.y, 0, 1.0);
    gl_Position = position;
}