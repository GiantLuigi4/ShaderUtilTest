#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DistortionSampler;
uniform sampler2D DistortionDepthSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

void main() {
    fragColor = texture(DiffuseSampler, texCoord);
    float fragDepth = texture(DiffuseDepthSampler, texCoord).x;
    float distortDepth = texture(DistortionDepthSampler, texCoord).x;
    if (distortDepth < fragDepth) {
        fragColor.r = fragColor.g = fragColor.b = max(fragColor.r, max(fragColor.g, fragColor.b));
    }
}
