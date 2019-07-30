/*
Utilities for stereo soundfield analysis.

Authors:
Jo Anderson     j.anderson@ambisonictoolkit.net
Michael McCrea  mtm5@uw.edu
2018
*/

StereoPower {
	*ar { |sterIn, numsamp=40, maxsamp=400|
		if (sterIn.size !=2) { Error("StereoPower input is not two channels").throw };

		^MovingAverage.power(sterIn, numsamp, maxsamp).sum
	}
}


StereoCorrelation {

	*ar { |sterIn, numsamp=40, maxsamp=400, reg = (-180.0.dbamp)|
		var power, mulLR;

		power = StereoPower.ar(sterIn, numsamp, maxsamp);
		mulLR = MovingAverage.ar(sterIn.product, numsamp, maxsamp);

		^(2 * mulLR) / (power + reg)
	}
}

StereoBalance {

	*ar { |sterIn, numsamp=40, maxsamp=400, reg = (-180.0.dbamp)|
		var power, diff;

		power = StereoPower.ar(sterIn, numsamp, maxsamp);
		diff = (MovingAverage.power(sterIn, numsamp, maxsamp) * [1, -1]).sum;

		^diff / (power + reg)
	}
}

// 0.5 * atan(balance / correlation)
StereoAngle {

	*ar { |sterIn, numsamp=40, maxsamp=400, reg = (-180.0.dbamp)|
		var diff, mulLR;

		diff = (MovingAverage.power(sterIn, numsamp, maxsamp) * [1, -1]).sum;
		mulLR = MovingAverage.ar(sterIn.product, numsamp, maxsamp);

		^0.5 * atan(diff / (2 * mulLR + reg))
	}
}

// Returned vector length is the cosine of the phase
// difference between L and R.
// hypot(balance, correlation)
StereoLocalization {

	*ar { |sterIn, numsamp=40, maxsamp=400, reg = (-180.0.dbamp)|
		var powLR, powSter, mulLR, diff;

		powLR = MovingAverage.power(sterIn, numsamp, maxsamp);
		powSter = powLR.sum;
		mulLR = MovingAverage.ar(sterIn.product, numsamp, maxsamp);

		diff = (powLR * [1, -1]).sum;

		^((2 * mulLR).squared + diff.squared).sqrt / (powSter + reg);

	}
}

/*
test:
StereoAngle
using sin/cos pan law, stereoAngle should return same input to panner

StereoLocalization
two in-phase sinusoids on left and right, return 1
sinusoids on left and 0 right, return 1
sinusoids on right and 0 left, return 1
two quadrature oscillators on l/r, return 0
*/