#version 330
in vec2 inPosition; // input from the vertex buffer
out vec3 color; // output from this shader to the next pipeline stage
uniform mat4 mat; // variable constant for all vertices in a single draw

void main() {
    color.xyz = vec3(0.8);
	gl_Position = mat * vec4(inPosition, 0.0, 1.0);
}
