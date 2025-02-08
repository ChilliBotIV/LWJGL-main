#version 330

layout (location = 0) in vec2 position;
in vec3 colour;
out vec3 vcolour;
uniform vec2 offset;
uniform float scale;
in vec2 Tex;
out vec2 TexCoord;

void main() {

    vec2 scaledPosition = (position * scale + offset) / vec2(320, 240);
    scaledPosition -= vec2(1,1);

    gl_Position = vec4(scaledPosition, 0, 1);
    vcolour = colour;
    TexCoord = Tex;
}