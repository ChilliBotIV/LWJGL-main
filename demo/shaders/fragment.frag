#version 330

out vec4 outputColor;
in vec3 vcolour;
in vec2 TexCoord;
uniform sampler2D mainTexture;
const float ambientStrength = 0.5;
const vec3 ambientColour = vec3(1, 0, 1);


void main() {
   outputColor = vec4(vcolour, 1.0f);
   outputColor = texture(mainTexture, TexCoord);

   vec3 ambient = ambientColour * ambientStrength;
   vec3 result = ambient * vcolour;
   outputColor = vec4(result, 1.0);
}