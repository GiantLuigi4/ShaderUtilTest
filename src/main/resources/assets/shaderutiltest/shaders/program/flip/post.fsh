#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DistortionSampler;
uniform sampler2D DistortionDepthSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    float fragDepth = texture(DiffuseDepthSampler, texCoord).x;
    float distortDepth = texture(DistortionDepthSampler, texCoord).x;
    if (distortDepth < fragDepth) {
        fragColor = texture(DiffuseSampler, texCoord *  vec2(1, -1) + vec2(0, 1));
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}
