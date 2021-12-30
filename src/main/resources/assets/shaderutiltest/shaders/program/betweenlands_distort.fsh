#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DistortionSampler;

uniform vec4 ColorModulate;
uniform float Ticks;

in vec2 texCoord;

out vec4 fragColor;

// src: https://github.com/Angry-Pixel/The-Betweenlands/blob/443521a35b187d41ab919142310cb20acdc77ea8/resources/assets/thebetweenlands/shaders/mc/program/blshader.fsh#L198
//Calculates the fragment world position (relative to camera)
vec3 getFragPos(sampler2D depthMap) {
    //Using the texture coordinate and the depth, the original vertex in world space coordinates can be calculated
    //The depth value from the depth buffer is not linear
    float zBuffer = texture2D(depthMap, texCoord).x;
    //float fragDepth = pow(zBuffer, 2);
    float fragDepth = zBuffer * 2.0F - 1.0F;

    //Calculate fragment world position relative to the camera position
    vec4 fragRelPos = vec4(texCoord.xy * 2.0F - 1.0F, fragDepth, 1.0F); // * u_INVMVP; // TODO
    fragRelPos.xyz /= fragRelPos.w;

    return fragRelPos.xyz;
}

void main(){
    vec4 distortion = texture(DistortionSampler, texCoord);
    float distort = distortion.x;

    vec3 fragPos = getFragPos(DiffuseDepthSampler);

    vec3 u_camPos = vec3(0);

    float distortionMultiplier = distort.x;

    vec4 color = vec4(0);

    float fragDistortion = (fragPos.y + u_camPos.y + (cos(fragPos.x + u_camPos.x) * sin(fragPos.z + u_camPos.z))) * 5.0F;
    color += vec4(texture2D(DiffuseSampler, v_texCoord + vec2(sin(fragDistortion + Ticks / 300.0F) / 800.0F, 0.0F) * distortionMultiplier));

//    fragColor = texture(DiffuseSampler, texCoord + distort) * ColorModulate;
    fragColor = color;
}