#version 330 core

in vec2 inPosition;

out vec3 fragmentPos;
flat out int fragmentTextureID;
out vec2 fragmentTexCoord;
out vec4 debug;

uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;
uniform sampler2D datamap;
uniform int size;

void main() {
    vec2 posOff = vec2(int(cameraPos.x)+inPosition.x, int(cameraPos.y)+inPosition.y);
    vec2 datapos;
    if (posOff.x <0 || posOff.y <0){
        datapos = vec2(0.0);
    }else{
        datapos = posOff/2048;
    }
    float z = texture(datamap, datapos).x*255;
    int res = int(texture(datamap, datapos).y*255);
    int texPosType = int(res & 3);

    if (texPosType == 0) fragmentTexCoord = vec2(0,0);
    else if (texPosType == 1) fragmentTexCoord = vec2(0,1);
    else if (texPosType == 2) fragmentTexCoord = vec2(1,1);
    else if (texPosType == 3) fragmentTexCoord = vec2(1,0);

    vec4 position = vec4(posOff.x, posOff.y, z, 1.0);
    debug = position;
    gl_Position = projection * view  * position;
    fragmentPos = position.xyz;
    fragmentTextureID = int(texture(datamap, datapos).z*255);
}
