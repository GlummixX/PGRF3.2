#version 330 core
in vec3 fragmentPos;
flat in int fragmentTextureID;
in vec2 fragmentTexCoord;

out vec4 outColor;
void main() {
    outColor.rgb = vec3(0.8);
    outColor.a = 1.0;
}