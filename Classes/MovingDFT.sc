/*
	A DFT with a variable window size.
	Requires SC3-Plugins (QuantityUGens: MovingAverage)

	Authors:
	Jo Anderson     j.anderson@ambisonictoolkit.net
	Michael McCrea  mtm5@uw.edu
	2018
*/

/*
	NOTE: max samples of the moving averaging are set based on freq and cpw
	so freq and cpw shouldn't be modulated such that freq and cpw exceed the
	that initial window size
*/

MovingDFT {

	//     in:  mono signal
	//    cpw:  cycles per window
	// maxwin:  optional, max analysis window size in seconds,
	//          if nil, set to initial numsamps calculation
	*ar { |in, freq, cpw = 2, maxWinSamps|
		var numsamps, maxsamps, real, imag, comp;

		numsamps = (SampleRate.ir/freq * cpw).ceil;
		maxsamps = maxWinSamps ?? { numsamps };

		#real, imag = MovingAverage.ar(
			in * SinOsc.ar(freq, [pi/2, 0]),
			numsamps,
			maxsamps
		);
		comp = Complex.new(real, imag);

		// ^[real, imag]
		^[comp.magnitude, comp.phase]
	}
}

MovingDFTReal {

	*ar { |in, freq, cpw = 2|
		var numsamps = (SampleRate.ir/freq * cpw).ceil;

		// real: cosine
		^MovingAverage.ar(
			in * SinOsc.ar(freq, pi/2),
			numsamps,
			numsamps
		);
	}
}

MovingDFTImag {

	*ar { |in, freq, cpw = 2|
		var numsamps = (SampleRate.ir/freq * cpw).ceil;

		// imaginary: sine
		^MovingAverage.ar(
			in * SinOsc.ar(freq, 0),
			numsamps,
			numsamps
		);
	}
}


/*

// ~~~~~~~
   SCRATCH
// ~~~~~~~

real
imag
mag
phase

MovingADFTReal
MovingADFTImag

// TODO:
MovingADFTmag
MovingADFTpha
MovingADFTCart
MovingADFTPol


((
~sd = CtkSynthDef(\movingDFT, { arg outbus=2, inFreq = 300, cpw = 8, probeFreq = 3000, smoothSamps = 100;
	var sine, dft;

	// the input signal: a sine wave
	// sine = SinOsc.ar(inFreq);
	// sine = SinOsc.ar(MouseX.kr(200, 15000).poll);
	sine = SinOsc.ar(LFTri.kr(15.reciprocal).range(200, 5000).poll);

	// dft = MovingDFT.ar(sine, probeFreq, cpw); // max window defaults to the size inititalized with cpw and freq
	dft = MovingDFT.ar(sine, probeFreq, cpw, (s.sampleRate/3000 * 8).ceil); // set max window size explicitly

	// smooth the dft further for plotting
	dft = MovingAverage.ar(dft, smoothSamps, SampleRate.ir);

	Out.kr(outbus, A2K.kr(dft));
})
)

(
~c = Bus.control(s, 1); // 1 channel, just magnitude now
)

p = LivePlotter(~c).autoYRange_(1)
// p = LivePlotter(~a).autoYRange_(1).overlay_(1).waveColors_([Color.red, Color.blue])


x = ~sd.note().outbus_(~c.index).probeFreq_(3000).play
x.inFreq = 3000
x.cpw = 1;
x.cpw = 4; // larger window, narrower band, lower sidelobes, but more time smearing
x.cpw = 8; // larger window, narrower band, lower sidelobes, but more time smearing
x.cpw = 10; // clipped at the max cpw (8), no change

x.smoothSamps = 0.005 * s.sampleRate
// x.smoothSamps = 0.05 * s.sampleRate/s.options.blockSize
x.smoothSamps = 3

x.free
s.scope

*/
