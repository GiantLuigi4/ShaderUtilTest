#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DistortionSampler;
uniform sampler2D DistortionDepthSampler;

in vec2 texCoord;

uniform vec2 InSize;

out vec4 fragColor;

// https://github.com/BloomhouseMC/Phasmophobia/blob/main/src/main/resources/assets/phasmophobia/shaders/program/flashlight.fsh
float near = 0.001;
float far  = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    float fragDepth = texture(DiffuseDepthSampler, texCoord).x;
    float distortDepth = texture(DistortionDepthSampler, texCoord).x;
    if (distortDepth < fragDepth) {
        fragColor = texture(DiffuseDepthSampler, texCoord);
        fragColor.r = LinearizeDepth(fragColor.r);
        fragColor.g = fragColor.b = fragColor.r;
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}
