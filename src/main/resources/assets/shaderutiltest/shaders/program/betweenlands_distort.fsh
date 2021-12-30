// inspired by : https://github.com/Angry-Pixel/The-Betweenlands/blob/443521a35b187d41ab919142310cb20acdc77ea8/resources/assets/thebetweenlands/shaders/mc/program/blshader.fsh
#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D DistortionSampler;
uniform sampler2D DistortionDepthSampler;

uniform float timeMillis;

uniform mat4 invProj;

uniform vec3 camPos;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 distortion = texture(DistortionSampler, texCoord);
    float distort = distortion.x;

    vec3 fragPos = vec3(texCoord.xy * 2 - 1, 0);

    float distortionMultiplier = distort * 5;

    vec4 color = vec4(0);

    float dOff = (sin(distortionMultiplier / 5));
    float fragDepth = texture(DiffuseDepthSampler, texCoord).x;
    float distortDepth = texture(DistortionDepthSampler, texCoord).x;
    // TODO: figure out how to make distortion depth into a sphere

    if (distortDepth < fragDepth) {
        float distortX = fragPos.y + camPos.y + cos(fragPos.x + camPos.x) + sin(fragPos.z + camPos.z);
        float distortY = fragPos.x + camPos.x + sin(fragPos.y + camPos.y) + cos(fragPos.z + camPos.z);
        color += texture(DiffuseSampler, texCoord + vec2(
            cos((distortX * 10) + timeMillis / 300),
            sin((distortY * 10) + timeMillis / 300)
        ) * distortionMultiplier / 1000.0F);
    } else {
        color += texture(DiffuseSampler, texCoord);
    }

    color.a = 1;

    fragColor = color;
}