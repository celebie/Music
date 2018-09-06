
import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;

public class cubes extends PApplet
{
//	Declare variables & objects
	Minim minim;
	AudioPlayer song;
	FFT fft;
	float specLow;
	float specMid;
	float specHi;
	float scoreLow;
	float scoreMid;
	float scoreHi;
	float oldScoreLow;
	float oldScoreMid;
	float oldScoreHi;
	float scoreDecreaseRate;
	int nbWaves;
	AudioWaves[] waves;
	
//	Define what variables & objects values are
	public cubes() {
		this.specLow = 0.03f;
		this.specMid = 0.125f;
		this.specHi = 0.2f;
		this.scoreLow = 0.0f;
		this.scoreMid = 0.0f;
		this.scoreHi = 0.0f;
		this.oldScoreLow = this.scoreLow;
		this.oldScoreMid = this.scoreMid;
		this.oldScoreHi = this.scoreHi;
		this.scoreDecreaseRate = 25.0f;
		this.nbWaves = 500;
	}
	
//	main setup for the application (Loading the song file, Defining what the audiowaves are
    public void setup() {
        this.minim = new Minim((Object)this);
        this.song = this.minim.loadFile("/songs/rooftops.mp3");
        this.fft = new FFT(this.song.bufferSize(), this.song.sampleRate());
        
        this.waves = new AudioWaves[this.nbWaves];
        for (int i = 0; i < this.nbWaves; i += 4) {
            this.waves[i] = new AudioWaves(0.0f, this.height / 2, 10.0f, this.height);
        }
        for (int i = 1; i < this.nbWaves; i += 4) {
            this.waves[i] = new AudioWaves(this.width, this.height / 2, 10.0f, this.height);
        }
        for (int i = 2; i < this.nbWaves; i += 4) {
            this.waves[i] = new AudioWaves(this.width / 2, this.height, this.width, 10.0f);
        }
        for (int i = 3; i < this.nbWaves; i += 4) {
            this.waves[i] = new AudioWaves(this.width / 2, 0.0f, this.width, 10.0f);
        }
        this.background(0);
        this.song.play(0);
        this.song.loop();
    }
    
//    Drawing the audio waves onto the screen based off the intensity of the song, using the minim library 
    public void draw() {
        this.fft.forward(this.song.mix);
        
        this.oldScoreLow = this.scoreLow;
        this.oldScoreMid = this.scoreMid;
        this.oldScoreHi = this.scoreHi;
        this.scoreLow = 0.0f;
        this.scoreMid = 0.0f;
        this.scoreHi = 0.0f;
        for (int i = 0; i < this.fft.specSize() * this.specLow; ++i) {
            this.scoreLow += this.fft.getBand(i);
        }
        for (int i = (int)(this.fft.specSize() * this.specLow); i < this.fft.specSize() * this.specMid; ++i) {
            this.scoreMid += this.fft.getBand(i);
        }
        for (int i = (int)(this.fft.specSize() * this.specMid); i < this.fft.specSize() * this.specHi; ++i) {
            this.scoreHi += this.fft.getBand(i);
        }
        if (this.oldScoreLow > this.scoreLow) {
            this.scoreLow = this.oldScoreLow - this.scoreDecreaseRate;
        }
        if (this.oldScoreMid > this.scoreMid) {
            this.scoreMid = this.oldScoreMid - this.scoreDecreaseRate;
        }
        if (this.oldScoreHi > this.scoreHi) {
            this.scoreHi = this.oldScoreHi - this.scoreDecreaseRate;
        }
        final float scoreGlobal = 0.66f * this.scoreLow + 0.8f * this.scoreMid + 1.0f * this.scoreHi;
        this.background(this.scoreLow / 100.0f, this.scoreMid / 100.0f, this.scoreHi / 100.0f);
        for (int j = 0; j < this.nbWaves; ++j) {
            final float intensity = this.fft.getBand(j % (int)(this.fft.specSize() * this.specHi));
            this.waves[j].display(this.scoreLow, this.scoreMid, this.scoreHi, intensity, scoreGlobal);
        }
    }
	
//    Display in fullscreen
	public void settings() {
		this.fullScreen("processing.opengl.PGraphics3D");
	}
	
//	Run the main application
	public static void main(final String[] passedArgs) {
		final String[] appletArgs = { "cubes" };
		if(passedArgs != null) { PApplet.main(concat(appletArgs, passedArgs)); } else { PApplet.main(appletArgs); }
	}
	
//	Display the audio waves based off the intensitiy of the song
    class AudioWaves
    {
        float startingZ;
        float maxZ;
        float x;
        float y;
        float z;
        float sizeX;
        float sizeY;
        
        AudioWaves(final float x, final float y, final float sizeX, final float sizeY) {
            this.startingZ = -10000.0f;
            this.maxZ = 50.0f;
            this.x = x;
            this.y = y;
            this.z = cubes.this.random(this.startingZ, this.maxZ);
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }
        
        public void display(final float scoreLow, final float scoreMid, final float scoreHi, float intensity, final float scoreGlobal) {
            int displayColor = cubes.this.color(scoreLow * 0.67f, scoreMid * 0.67f, scoreHi * 0.67f, scoreGlobal);
            cubes.this.fill(displayColor, (scoreGlobal - 5.0f) / 1000.0f * (255.0f + this.z / 25.0f));
            cubes.this.noStroke();
            cubes.this.pushMatrix();
            cubes.this.translate(this.x, this.y, this.z);
            if (intensity > 100.0f) {
                intensity = 100.0f;
            }
            cubes.this.scale(this.sizeX * (intensity / 100.0f), this.sizeY * (intensity / 100.0f), 20.0f);
            cubes.this.box(1.0f);
            cubes.this.popMatrix();
            displayColor = cubes.this.color(scoreLow * 0.5f, scoreMid * 0.5f, scoreHi * 0.5f, scoreGlobal);
            cubes.this.fill(displayColor, scoreGlobal / 5000.0f * (255.0f + this.z / 25.0f));
            cubes.this.pushMatrix();
            cubes.this.translate(this.x, this.y, this.z);
            cubes.this.scale(this.sizeX, this.sizeY, 10.0f);
            cubes.this.box(1.0f);
            cubes.this.popMatrix();
            this.z += cubes.pow(scoreGlobal / 150.0f, 2.0f);
            if (this.z >= this.maxZ) {
                this.z = this.startingZ;
            }
        }
    }
}
