#version 460 core

// Quads
layout (quads, equal_spacing, ccw) in;

in vec2 uvsCoord[];
out vec2 uvs;
out vec3 fragPos;

uniform sampler2D datamap;
uniform mat4 view;
uniform mat4 projection;

float getZ(vec2 pos) {
    vec2 datapos;
    if (pos.x < 0 || pos.y < 0) {
        datapos = vec2(0.0);
    } else {
        datapos = pos.xy / 1024.;
    }
    return texture(datamap, datapos).x * 255;
}

float averageZ(vec2 pos) {
    float z = getZ(pos);
    float z1 = getZ(pos + vec2(1.0));
    float z2 = getZ(pos + vec2(1.0, -1.0));
    float z3 = getZ(pos + vec2(-1.0));
    float z4 = getZ(pos + vec2(-1.0, 1.0));
    if (abs(((z1 + z2 + z3 + z4) / 4) - z) >= 2) {
        z = (z1 + z2 + z3 + z4 + z) / 5;
    }
    return z;
}

void main() {
    float u = gl_TessCoord.x;
    float v = gl_TessCoord.y;

    vec2 uv0 = uvsCoord[0];
    vec2 uv1 = uvsCoord[1];
    vec2 uv2 = uvsCoord[2];
    vec2 uv3 = uvsCoord[3];

    vec2 leftUV = uv0 + v * (uv3 - uv0);
    vec2 rightUV = uv1 + v * (uv2 - uv1);
    vec2 texCoord = leftUV + u * (rightUV - leftUV);

    vec4 pos0 = gl_in[0].gl_Position;
    vec4 pos1 = gl_in[1].gl_Position;
    vec4 pos2 = gl_in[2].gl_Position;
    vec4 pos3 = gl_in[3].gl_Position;


    vec4 leftPos = pos0 + v * (pos3 - pos0);
    vec4 rightPos = pos1 + v * (pos2 - pos1);
    vec4 pos = leftPos + u * (rightPos - leftPos);

    pos.z = averageZ(pos.xy);

    fragPos = pos.xyz;

    gl_Position = projection * view * pos;
    uvs = texCoord;
}