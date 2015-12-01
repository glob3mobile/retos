/************************************************************************************

Filename	:	VrCubeWorld_Framework.cpp
Content		:	This sample uses the application framework.
Created		:	March, 2015
Authors		:	J.M.P. van Waveren

Copyright	:	Copyright 2015 Oculus VR, LLC. All Rights reserved.

*************************************************************************************/

#include "App.h"
#include "GuiSys.h"
#include "SceneView.h"
#include "OVR_Locale.h"
#include "SoundEffectContext.h"
#include <memory>

#if 0
	#define GL( func )		func; EglCheckErrors();
#else
	#define GL( func )		func;
#endif

/*
================================================================================

VrCubeWorld

================================================================================
*/

namespace OVR
{

static const int CPU_LEVEL			= 2;
static const int GPU_LEVEL			= 3;
static const int NUM_INSTANCES		= 1500;

static const char VERTEX_SHADER[] =
#if defined( OVR_OS_ANDROID )
	"#version 300 es\n"
#endif
	"in vec3 Position;\n"
	"in vec4 VertexColor;\n"
	"in mat4 VertexTransform;\n"
	"uniform mat4 Viewm;\n"
	"uniform mat4 Projectionm;\n"
	"out vec4 fragmentColor;\n"
	"void main()\n"
	"{\n"
	"	gl_Position = Projectionm * ( Viewm * ( VertexTransform * vec4( Position, 1.0 ) ) );\n"
	"	fragmentColor = VertexColor;\n"
	"}\n";

static const char FRAGMENT_SHADER[] =
#if defined( OVR_OS_ANDROID )
	"#version 300 es\n"
#endif
	"in lowp vec4 fragmentColor;\n"
	"out lowp vec4 outColor;\n"
	"void main()\n"
	"{\n"
	"	outColor = fragmentColor;\n"
	"}\n";

// setup Cube
struct ovrCubeVertices
{
	Vector3f positions[8];
	Vector4f colors[8];
};

static ovrCubeVertices cubeVertices =
{
	// positions
	{
		Vector3f( -1.0f, +1.0f, -1.0f ), Vector3f( +1.0f, +1.0f, -1.0f ), Vector3f( +1.0f, +1.0f, +1.0f ), Vector3f( -1.0f, +1.0f, +1.0f ),	// top
		Vector3f( -1.0f, -1.0f, -1.0f ), Vector3f( -1.0f, -1.0f, +1.0f ), Vector3f( +1.0f, -1.0f, +1.0f ), Vector3f( +1.0f, -1.0f, -1.0f )	// bottom
	},
	// colors
	{
		Vector4f( 1.0f, 0.0f, 1.0f, 1.0f ), Vector4f( 0.0f, 1.0f, 0.0f, 1.0f ), Vector4f( 0.0f, 0.0f, 1.0f, 1.0f ), Vector4f( 1.0f, 0.0f, 0.0f, 1.0f ),
		Vector4f( 0.0f, 0.0f, 1.0f, 1.0f ), Vector4f( 0.0f, 1.0f, 0.0f, 1.0f ), Vector4f( 1.0f, 0.0f, 1.0f, 1.0f ), Vector4f( 1.0f, 0.0f, 0.0f, 1.0f )
	},
};

static const unsigned short cubeIndices[36] =
{
	0, 1, 2, 2, 3, 0,	// top
	4, 5, 6, 6, 7, 4,	// bottom
	2, 6, 7, 7, 1, 2,	// right
	0, 4, 5, 5, 3, 0,	// left
	3, 5, 6, 6, 2, 3,	// front
	0, 1, 7, 7, 4, 0	// back
};


	class VrCubeWorld : public VrAppInterface
	{
	public:
		VrCubeWorld();
		~VrCubeWorld();

		virtual void 		Configure( ovrSettings & settings );

		virtual void		OneTimeInit( const char * fromPackage, const char * launchIntentJSON, const char * launchIntentURI );
		virtual void		OneTimeShutdown();
		virtual bool		OnKeyEvent( const int keyCode, const int repeatCount, const KeyEventType eventType );
		virtual Matrix4f	Frame( const VrFrame & vrFrame );
		virtual Matrix4f	DrawEyeView( const int eye, const float fovDegreesX, const float fovDegreesY, ovrFrameParms & frameParms );

		ovrLocale &			GetLocale() { return *Locale; }

		OvrSceneView		Scene;

		static float theYaw;
		static float thePitch;
		static float theRoll;

	private:
		ovrSoundEffectContext * SoundEffectContext;
		OvrGuiSys::SoundEffectPlayer * SoundEffectPlayer;
		OvrGuiSys *			GuiSys;
		ovrLocale *			Locale;
		unsigned int		Random;
		GlProgram			Program;
		GlGeometry			Cube;
		GLint				VertexTransformAttribute;
		GLuint				InstanceTransformBuffer;
		ovrVector3f			CubePositions[NUM_INSTANCES];
		ovrVector3f			CubeRotations[NUM_INSTANCES];
		ovrMatrix4f			CenterEyeViewMatrix;

		float				RandomFloat();
	};

	float VrCubeWorld::theYaw = 0;
	float VrCubeWorld::thePitch = 0;
	float VrCubeWorld::theRoll = 0;

	VrCubeWorld::VrCubeWorld() :
			SoundEffectContext( NULL ),
			SoundEffectPlayer( NULL ),
			GuiSys( OvrGuiSys::Create() ),
			Locale( NULL ),
			Random( 2 )
	{
		//CenterEyeViewMatrix = ovrMatrix4f_CreateIdentity();
	}

	VrCubeWorld::~VrCubeWorld()
	{
		OvrGuiSys::Destroy( GuiSys );
	}


	float VrCubeWorld::RandomFloat()
	{
		Random = 1664525L * Random + 1013904223L;
		unsigned int rf = 0x3F800000 | ( Random & 0x007FFFFF );
		return (*(float *)&rf) - 1.0f;
	}

	void VrCubeWorld::OneTimeInit( const char * fromPackageName, const char * launchIntentJSON, const char * launchIntentURI )
	{
		OVR_UNUSED( fromPackageName );
		OVR_UNUSED( launchIntentJSON );
		OVR_UNUSED( launchIntentURI );

		const ovrJava * java = app->GetJava();
		SoundEffectContext = new ovrSoundEffectContext( *java->Env, java->ActivityObject );
		SoundEffectContext->Initialize();
		SoundEffectPlayer = new OvrGuiSys::ovrDummySoundEffectPlayer();

		Locale = ovrLocale::Create( *app, "default" );

		String fontName;
		GetLocale().GetString( "@string/font_name", "efigs.fnt", fontName );
		GuiSys->Init( this->app, *SoundEffectPlayer, fontName.ToCStr(), &app->GetDebugLines() );

		//Old code
		const OvrStoragePaths & paths = app->GetStoragePaths();
		//Busca direcciones a carpeta de assets de oculus, creo.
		Array<String> SearchPaths;
		paths.PushBackSearchPathIfValid( EST_SECONDARY_EXTERNAL_STORAGE, EFT_ROOT, "RetailMedia/", SearchPaths );
		paths.PushBackSearchPathIfValid( EST_SECONDARY_EXTERNAL_STORAGE, EFT_ROOT, "", SearchPaths );
		paths.PushBackSearchPathIfValid( EST_PRIMARY_EXTERNAL_STORAGE, EFT_ROOT, "RetailMedia/", SearchPaths );
		paths.PushBackSearchPathIfValid( EST_PRIMARY_EXTERNAL_STORAGE, EFT_ROOT, "", SearchPaths );

		const char * scenePath = "Oculus/tuscany2.ovrscene";
		String SceneFile;
		//Si está esa escena, la carga.
		if ( GetFullPath( SearchPaths, scenePath, SceneFile ) )
		{
			MaterialParms materialParms;
			materialParms.UseSrgbTextureFormats = false;
			Scene.LoadWorldModel( SceneFile.ToCStr(), materialParms );
			Scene.SetYawOffset( -M_PI * 0.5f );
		}
		else
		{
			LOG( "OvrApp::OneTimeInit SearchPaths failed to find %s", scenePath );
		}


		// Create the program.
		/*
        Program = BuildProgram( VERTEX_SHADER, FRAGMENT_SHADER );
        VertexTransformAttribute = glGetAttribLocation( Program.program, "VertexTransform" );

        // Create the cube.
        VertexAttribs attribs;
        attribs.position.Resize( 8 );
        attribs.color.Resize( 8 );
        for ( int i = 0; i < 8; i++ )
        {
            attribs.position[i] = cubeVertices.positions[i];
            attribs.color[i] = cubeVertices.colors[i];
        }

        Array< TriangleIndex > indices;
        indices.Resize( 36 );
        for ( int i = 0; i < 36; i++ )
        {
            indices[i] = cubeIndices[i];
        }

        Cube.Create( attribs, indices );

        // Setup the instance transform attributes.
        GL( glBindVertexArray( Cube.vertexArrayObject ) );
        GL( glGenBuffers( 1, &InstanceTransformBuffer ) );
        GL( glBindBuffer( GL_ARRAY_BUFFER, InstanceTransformBuffer ) );
        GL( glBufferData( GL_ARRAY_BUFFER, NUM_INSTANCES * 4 * 4 * sizeof( float ), NULL, GL_DYNAMIC_DRAW ) );
        for ( int i = 0; i < 4; i++ )
        {
            GL( glEnableVertexAttribArray( VertexTransformAttribute + i ) );
            GL( glVertexAttribPointer( VertexTransformAttribute + i, 4, GL_FLOAT,
                                        false, 4 * 4 * sizeof( float ), (void *)( i * 4 * sizeof( float ) ) ) );
            GL( glVertexAttribDivisor( VertexTransformAttribute + i, 1 ) );
        }
        GL( glBindVertexArray( 0 ) );

        // Setup random cube positions and rotations.
        for ( int i = 0; i < NUM_INSTANCES; i++ )
        {
            volatile float rx, ry, rz;
            for ( ; ; )
            {
                rx = ( RandomFloat() - 0.5f ) * ( 50.0f + static_cast<float>( sqrt( NUM_INSTANCES ) ) );
                ry = ( RandomFloat() - 0.5f ) * ( 50.0f + static_cast<float>( sqrt( NUM_INSTANCES ) ) );
                rz = ( RandomFloat() - 0.5f ) * ( 50.0f + static_cast<float>( sqrt( NUM_INSTANCES ) ) );

                // If too close to 0,0,0
                if ( fabsf( rx ) < 4.0f && fabsf( ry ) < 4.0f && fabsf( rz ) < 4.0f )
                {
                    continue;
                }

                // Test for overlap with any of the existing cubes.
                bool overlap = false;
                for ( int j = 0; j < i; j++ )
                {
                    if (	fabsf( rx - CubePositions[j].x ) < 4.0f &&
                            fabsf( ry - CubePositions[j].y ) < 4.0f &&
                            fabsf( rz - CubePositions[j].z ) < 4.0f )
                    {
                        overlap = true;
                        break;
                    }
                }

                if ( !overlap )
                {
                    break;
                }
            }

            // Insert into list sorted based on distance.
            int insert = 0;
            const float distSqr = rx * rx + ry * ry + rz * rz;
            for ( int j = i; j > 0; j-- )
            {
                const ovrVector3f * otherPos = &CubePositions[j - 1];
                const float otherDistSqr = otherPos->x * otherPos->x + otherPos->y * otherPos->y + otherPos->z * otherPos->z;
                if ( distSqr > otherDistSqr )
                {
                    insert = j;
                    break;
                }
                CubePositions[j] = CubePositions[j - 1];
                CubeRotations[j] = CubeRotations[j - 1];
            }

            CubePositions[insert].x = rx;
            CubePositions[insert].y = ry;
            CubePositions[insert].z = rz;

            CubeRotations[insert].x = RandomFloat();
            CubeRotations[insert].y = RandomFloat();
            CubeRotations[insert].z = RandomFloat();
        }
         */
	}

	void VrCubeWorld::OneTimeShutdown()
	{
		delete SoundEffectPlayer;
		SoundEffectPlayer = NULL;

		delete SoundEffectContext;
		SoundEffectContext = NULL;

		/*DeleteProgram( Program );
        Cube.Free();
        GL( glDeleteBuffers( 1, &InstanceTransformBuffer ) );*/
	}

	void VrCubeWorld::Configure( ovrSettings & settings )
	{
		/*settings.PerformanceParms.CpuLevel = CPU_LEVEL;
        settings.PerformanceParms.GpuLevel = GPU_LEVEL;
        settings.EyeBufferParms.multisamples = 4;*/
		settings.PerformanceParms.CpuLevel = 2;
		settings.PerformanceParms.GpuLevel = 2;
	}

	bool VrCubeWorld::OnKeyEvent( const int keyCode, const int repeatCount, const KeyEventType eventType )
	{
		if ( GuiSys->OnKeyEvent( keyCode, repeatCount, eventType ) )
		{
			return true;
		}
		return false;
	}

	Matrix4f VrCubeWorld::Frame( const VrFrame & vrFrame )
	{
		// Player movement.
		Scene.Frame( vrFrame, app->GetHeadModelParms() );

		// Update GUI systems after the app frame, but before rendering anything.
		GuiSys->Frame( vrFrame, Scene.GetCenterEyeViewMatrix() );

		//Updating my headset data;
		theYaw = Scene.GetEyeYaw();
		thePitch = Scene.GetEyePitch();
		theRoll = Scene.GetEyeRoll();

		return Scene.GetCenterEyeViewMatrix();

		/*Vector3f currentRotation;
        currentRotation.x = (float)( vrFrame.PredictedDisplayTimeInSeconds );
        currentRotation.y = (float)( vrFrame.PredictedDisplayTimeInSeconds );
        currentRotation.z = (float)( vrFrame.PredictedDisplayTimeInSeconds );

        // Update the instance transform attributes.
        GL( glBindBuffer( GL_ARRAY_BUFFER, InstanceTransformBuffer ) );
        GL( Matrix4f * cubeTransforms = (Matrix4f *) glMapBufferRange( GL_ARRAY_BUFFER, 0,
                    NUM_INSTANCES * sizeof( Matrix4f ), GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT ) );
        for ( int i = 0; i < NUM_INSTANCES; i++ )
        {
            const Matrix4f rotation =	Matrix4f::RotationX( CubeRotations[i].x * currentRotation.x ) *
                                        Matrix4f::RotationY( CubeRotations[i].y * currentRotation.y ) *
                                        Matrix4f::RotationZ( CubeRotations[i].z * currentRotation.z );
            const Matrix4f translation = Matrix4f::Translation(
                                            CubePositions[i].x,
                                            CubePositions[i].y,
                                            CubePositions[i].z );
            const Matrix4f transform = translation * rotation;
            cubeTransforms[i] = transform.Transposed();
        }
        GL( glUnmapBuffer( GL_ARRAY_BUFFER ) );
        GL( glBindBuffer( GL_ARRAY_BUFFER, 0 ) );

        CenterEyeViewMatrix = vrapi_GetCenterEyeViewMatrix( &app->GetHeadModelParms(), &vrFrame.Tracking, NULL );

        // Update GUI systems last, but before rendering anything.
        GuiSys->Frame( vrFrame, CenterEyeViewMatrix );

        return CenterEyeViewMatrix;*/


	}

	Matrix4f VrCubeWorld::DrawEyeView( const int eye, const float fovDegreesX, const float fovDegreesY, ovrFrameParms & frameParms )
	{
		OVR_UNUSED( frameParms );

		const Matrix4f viewMatrix = Scene.GetEyeViewMatrix( eye );
		const Matrix4f projectionMatrix = Scene.GetEyeProjectionMatrix( eye, fovDegreesX, fovDegreesY );
		const Matrix4f eyeViewProjection = Scene.DrawEyeView( eye, fovDegreesX, fovDegreesY );

		frameParms.ExternalVelocity = Scene.GetExternalVelocity();
		frameParms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Flags |= VRAPI_FRAME_LAYER_FLAG_CHROMATIC_ABERRATION_CORRECTION;

		GuiSys->RenderEyeView( Scene.GetCenterEyeViewMatrix(), viewMatrix, projectionMatrix );

		return eyeViewProjection;

		//New VrTemplate code instead


		/*const Matrix4f eyeViewMatrix = vrapi_GetEyeViewMatrix( &app->GetHeadModelParms(), &CenterEyeViewMatrix, eye );
        const Matrix4f eyeProjectionMatrix = ovrMatrix4f_CreateProjectionFov( fovDegreesX, fovDegreesY, 0.0f, 0.0f, 1.0f, 0.0f );
        const Matrix4f eyeViewProjection = eyeProjectionMatrix * eyeViewMatrix;

        GL( glClearColor( 0.125f, 0.0f, 0.125f, 1.0f ) );
        GL( glClear( GL_COLOR_BUFFER_BIT ) );
        GL( glUseProgram( Program.program ) );
        GL( glUniformMatrix4fv( Program.uView, 1, GL_TRUE, eyeViewMatrix.M[0] ) );
        GL( glUniformMatrix4fv( Program.uProjection, 1, GL_TRUE, eyeProjectionMatrix.M[0] ) );
        GL( glBindVertexArray( Cube.vertexArrayObject ) );
        GL( glDrawElementsInstanced( GL_TRIANGLES, Cube.indexCount, GL_UNSIGNED_SHORT, NULL, NUM_INSTANCES ) );
        GL( glBindVertexArray( 0 ) );
        GL( glUseProgram( 0 ) );

        GuiSys->RenderEyeView( CenterEyeViewMatrix, eyeViewMatrix, eyeProjectionMatrix );

        frameParms.Layers[VRAPI_FRAME_LAYER_TYPE_WORLD].Flags |= VRAPI_FRAME_LAYER_FLAG_CHROMATIC_ABERRATION_CORRECTION;

        return eyeViewProjection;*/
	}

} // namespace OVR

#if defined( OVR_OS_ANDROID )
extern "C"
{

long Java_com_example_vrcamera_MainActivity_nativeSetAppInterface( JNIEnv *jni, jclass clazz, jobject activity,
	jstring fromPackageName, jstring commandString, jstring uriString )
{
	// This is called by the java UI thread.
	LOG( "nativeSetAppInterface" );
	return (new OVR::VrCubeWorld())->SetActivity( jni, clazz, activity, fromPackageName, commandString, uriString );
}

} // extern "C"

#endif
