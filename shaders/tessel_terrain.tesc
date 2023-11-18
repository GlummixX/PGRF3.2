#version 460 core

layout (vertices = 4) out;

in vec2 uvs[];
out vec2 uvsCoord[];

layout (location = 0) uniform vec3 cameraPos;

const int MIN_TES = 2;
uniform int maxTess;
const float MIN_DIST = 75.;
const float MAX_DIST = 350.;

void main()
{
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    uvsCoord[gl_InvocationID] = uvs[gl_InvocationID];

    if (gl_InvocationID == 0)
    {
        vec2 center0 = (gl_in[0].gl_Position.xy + gl_in[3].gl_Position.xy) / 2.0; // left side
        vec2 center1 = (gl_in[1].gl_Position.xy + gl_in[0].gl_Position.xy) / 2.0; // bot side
        vec2 center2 = (gl_in[2].gl_Position.xy + gl_in[1].gl_Position.xy) / 2.0; // right side
        vec2 center3 = (gl_in[3].gl_Position.xy + gl_in[2].gl_Position.xy) / 2.0; // top side

        float dist0 = max(MIN_DIST, length(cameraPos.xy - center0));
        float dist1 = max(MIN_DIST, length(cameraPos.xy - center1));
        float dist2 = max(MIN_DIST, length(cameraPos.xy - center2));
        float dist3 = max(MIN_DIST, length(cameraPos.xy - center3));

        int tes0 = int(mix(maxTess, MIN_TES, clamp(dist0 / MAX_DIST, 0.0, 1.0)));
        int tes1 = int(mix(maxTess, MIN_TES, clamp(dist1 / MAX_DIST, 0.0, 1.0)));
        int tes2 = int(mix(maxTess, MIN_TES, clamp(dist2 / MAX_DIST, 0.0, 1.0)));
        int tes3 = int(mix(maxTess, MIN_TES, clamp(dist3 / MAX_DIST, 0.0, 1.0)));

        gl_TessLevelOuter[0] = tes0; // left for quads
        gl_TessLevelOuter[1] = tes1; // bot for quads
        gl_TessLevelOuter[2] = tes2; // right for quads
        gl_TessLevelOuter[3] = tes3; // top for quads

        gl_TessLevelInner[0] = max(tes1, tes3); // top bot for quads
        gl_TessLevelInner[1] = max(tes0, tes2); // left right for quads
    }
}