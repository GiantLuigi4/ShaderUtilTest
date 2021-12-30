#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    fragColor = texture(DiffuseSampler, texCoord);
}
