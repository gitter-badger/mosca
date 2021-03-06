/*
Mosca: SuperCollider class by Iain Mott, 2016. Licensed under a
Creative Commons Attribution-NonCommercial 4.0 International License
http://creativecommons.org/licenses/by-nc/4.0/
The class makes extensive use of the Ambisonic Toolkit (http://www.ambisonictoolkit.net/)
by Joseph Anderson and the Automation quark
(https://github.com/neeels/Automation) by Neels Hofmeyr.
Required Quarks : Automation, Ctk, XML and  MathLib
Required classes:
SC Plugins: https://github.com/supercollider/sc3-plugins
User must set up a project directory with subdirectoties "rir" and "auto"
RIRs should have the first 100 or 120ms silenced to act as "tail" reverberators
and must be placed in the "rir" directory.
Run help on the "Mosca" class in SuperCollider for detailed information
and code examples. Further information and sample RIRs and B-format recordings
may be downloaded here: http://escuta.org/mosca
*/


Mosca {
	var <nfontes,
	revGlobal, nonAmbi2FuMa, convertor,
	libnumbox, <>control,
	globDec,
	sysex, mmcslave,
	synthRegistry, busini, ncan,
	aux1, aux2, aux3, aux4, aux5,  // aux slider values
	triggerFunc, stopFunc,
	scInBus,
	fumabus,
	insertFlag,
	insertBus,
	<dur,
	<>looping,
	serport,
	offsetheading,
	libbox, lpcheck, dstrvbox, hwncheck, scncheck,
	spcheck, dfcheck,
	ncanbox, businibox,
	espacializador, synt,
	tfield,
	autoloopval,
	<>autoloop,
	streamdisk,
	streambuf, // streamrate, // apparently unused
	origine,
	oxnumboxProxy, oynumboxProxy, oznumboxProxy,
	pitch, pitchnumboxProxy,
	roll, rollnumboxProxy,
	heading, headingnumboxProxy,
	// head tracking
	trackarr, trackarr2, tracki, trackPort,
	// track2arr, track2arr2, track2i,
	headingOffset,
	cartval, spheval,
	recchans, recbus,
	// mark1, mark2,	// 4 number arrays for marker data // apparently unused

	// MOVED FROM the the gui method/////////////////////////

	cbox, clev, angle, ncanais, audit, gbus, gbfbus, n3dbus,
	gbixfbus, nonambibus,
	playEspacGrp, glbRevDecGrp,
	level, lp, lib, libName, convert, dstrv, dstrvtypes, clsrv,
	clsRvtypes,
	winCtl, originCtl, hwCtl,
	xbox, ybox, sombuf, sbus, mbus,
	rbox, abox, vbox, gbox, lbox, dbox, dpbox, zbox,
	a1check, a2check, a3check, a4check, a5check, a1box, a2box, a3box,
	a4box, a5box,
	stcheck,

	//oxbox, oybox, ozbox,
	//funcs, // apparently unused
	//lastx, lasty, // apparently unused
	zlev, znumbox, zslider,
	glev,
	lslider,
	llev, rlev, dlev,
	dplev,
	auxslider1, auxslider2, auxslider3, auxslider4, auxslider5,
	auxbutton1, auxbutton2, auxbutton3, auxbutton4, auxbutton5,
	aux1numbox, aux2numbox, aux3numbox, aux4numbox, aux5numbox,
	a1but, a2but, a3but, a4but, a5but,

	loopcheck, dstReverbox, clsReverbox, hwInCheck,
	hwn, scInCheck, scn,
	spreadcheck,
	diffusecheck, sp, df,

	hdtrk,

	atualizarvariaveis, updateSynthInArgs,

	<>runTriggers, <>runStops, <>runTrigger, <>runStop,
	// isRec, // apparently unused

	/////////////////////////////////////////////////////////

	// NEW PROXY VARIABLES /////////////

	<>rboxProxy, <>cboxProxy, <>aboxProxy, <>vboxProxy, <>gboxProxy, <>lboxProxy,
	<>dboxProxy, <>dpboxProxy, <>zboxProxy, <>yboxProxy, <>xboxProxy,
	<>a1checkProxy, <>a2checkProxy, <>a3checkProxy, <>a4checkProxy, <>a5checkProxy,
	<>a1boxProxy, <>a2boxProxy, <>a3boxProxy, <>a4boxProxy, <>a5boxProxy,
	<>stcheckProxy, <>tfieldProxy, <>libboxProxy, <>lpcheckProxy, <>dstrvboxProxy,
	<>clsrvboxProxy, <>hwncheckProxy, <>scncheckProxy, <>dfcheckProxy,
	<>spcheckProxy, <>ncanboxProxy, <>businiboxProxy,

	grainrate, ratebox, <>rateboxProxy, // setable granular rate
	winsize, winbox, <>winboxProxy, // setable granular window size
	winrand, randbox, <>randboxProxy,
	// setable granular window size random factor

	rm, rmbox, <>rmboxProxy, // setable local room size
	dm, dmbox, <>dmboxProxy, // setable local dampening

	clsrm, clsrmbox, <>clsrmboxProxy, // setable global room size
	clsdm, clsdmbox, <>clsdmboxProxy, // setable global dampening

	<>masterlevProxy, masterslider,

	<parentOssiaNode,
	ossiaorient, ossiaorigine, ossiaplay, ossiatrasportLoop,
	ossiatransport, ossiaseekback, ossiarec, ossiacart, ossiasphe, ossiaaud,
	ossialoop, ossialib, ossialev, ossiadp, ossiacls, ossiaclsam,
	ossiaclsdel, ossiaclsdec, ossiadst, ossiadstam, ossiadstdel, ossiadstdec,
	ossiaangle, ossiarot, ossiadir, ossiactr, ossiaspread, ossiadiff,
	ossiaCartBack, ossiaSpheBack, ossiarate, ossiawin, ossiarand, ossiamaster,
	ossiaMasterPlay, ossiaMasterLib, ossiaMasterRev;

	/////////////////////////////////////////


	classvar server,
	rirWspectrum, rirXspectrum, rirYspectrum, rirZspectrum, rirA12Spectrum,
	rirFLUspectrum, rirFRDspectrum, rirBLDspectrum, rirBRUspectrum,
	rirList, irSpecPar, wxyzSpecPar, zSpecPar, wSpecPar,
	spatList = #["Ambitools","HoaLib","ADTB","ATK","BF-FMH","Josh","VBAP"],
	// list of spat libs
	lastN3D = 2, // last N3D lib index
	lastFUMA = 5, // last FUMA lib index
	playList = #["File","HWBus","SWBus","Stream"],
	b2a, a2b, n2m,
	blips,
	maxorder,
	convert_fuma,
	convert_n3d,
	convert_direct,
	azimuths, radiusses, elevations,
	numoutputs,
	longest_radius, highest_elevation, lowest_elevation,
	vbap_buffer,
	soa_a12_decoder_matrix, soa_a12_encoder_matrix,
	cart, spher, foa_a12_decoder_matrix,
	width, halfwidth, height, halfheight, novoplot, updateGuiCtl,
	lastGui, guiInt,
	lastAutomation = nil,
	firstTime,
	isPlay = false,
	playingBF,
	currentsource,
	guiflag, baudi,
	watcher, troutine, kroutine,
	updatesourcevariables, prjDr,
	plim = 120, // distance limit from origin where processes continue to run
	fftsize = 2048, halfPi = 1.5707963267949, rad2deg = 57.295779513082 ,
	offsetLag = 2.0,  // lag in seconds for incoming GPS data
	server, foaEncoderOmni, foaEncoderSpread, foaEncoderDiffuse;
	*new { arg projDir, nsources = 10, width = 800, dur = 180, rirBank,
		server = Server.local, parentOssiaNode, allCrtitical = false, decoder,
		maxorder = 1, speaker_array, outbus = 0, suboutbus, rawformat = \FuMa, rawoutbus,
		serport, offsetheading = 0, recchans = 2, recbus = 0, guiflag = true,
		guiint = 0.07, autoloop = false;

		^super.new.initMosca(projDir, nsources, width, dur, rirBank,
			server, parentOssiaNode, allCrtitical, decoder, maxorder, speaker_array,
			outbus, suboutbus, rawformat, rawoutbus, serport, offsetheading, recchans,
			recbus, guiflag, guiint, autoloop);
	}


	initMosca { | projDir, nsources, iwidth, idur, rirBank, iserver, iparentOssiaNode,
		allCrtitical, decoder, imaxorder, speaker_array, outbus, suboutbus,
		rawformat, rawoutbus, iserport, ioffsetheading, irecchans, irecbus,
		iguiflag, iguiint, iautoloop |

		var makeSynthDefPlayers, makeSpatialisers, subOutFunc, playInFunc,
		localReverbFunc, localReverbStereoFunc, //localReverbBFormatFunc,
		perfectSphereFunc, bfOrFmh, spatFuncs, outPutFuncs,
		bFormNumChan = (imaxorder + 1).squared,
		// add the number of channels of the b format
		fourOrNine; // switch between 4 fuma and 9 ch Matrix

		nfontes = nsources;
		maxorder = imaxorder;
		server = iserver;
		parentOssiaNode = iparentOssiaNode;

		b2a = FoaDecoderMatrix.newBtoA;
		a2b = FoaEncoderMatrix.newAtoB;
		n2m = FoaEncoderMatrix.newHoa1();
		foaEncoderOmni = FoaEncoderMatrix.newOmni;
		foaEncoderSpread = FoaEncoderKernel.newSpread (subjectID: 6, kernelSize: 2048,
			server:server, sampleRate:server.sampleRate.asInteger);
		foaEncoderDiffuse = FoaEncoderKernel.newDiffuse (subjectID: 3, kernelSize: 2048,
			server:server, sampleRate:server.sampleRate.asInteger);

		if (maxorder > 1) {
			bfOrFmh = FMHEncode1;
			fourOrNine = 9;
		} {
			bfOrFmh = BFEncode1;
			fourOrNine = 4;
		};

		n3dbus = Bus.audio(server, bFormNumChan); // global b-format ACN-SN3D bus
		fumabus = Bus.audio(server, fourOrNine);
		gbus = Bus.audio(server, 1); // global reverb bus
		gbfbus = Bus.audio(server, fourOrNine); // global b-format bus
		gbixfbus = Bus.audio(server, fourOrNine); // global n3d b-format bus
		playEspacGrp = ParGroup.tail;
		glbRevDecGrp = Group.after(playEspacGrp);

		synthRegistry = Array.newClear(nfontes);
		insertFlag = Array.newClear(nfontes);
		insertBus = Array2D.new(2, nfontes);
		scInBus = Array.newClear(nfontes);

		server.sync;

		nfontes.do { | i |
			synthRegistry[i] = List[];

			scInBus[i] = Bus.audio(server, 1);

			insertBus[0, i] = Bus.audio(server, fourOrNine);
			insertBus[1, i] = Bus.audio(server, fourOrNine);

			insertFlag[i] = 0;
		};

		if (iwidth < 600) {
			width = 600;
		} {
			width = iwidth;
		};

		halfwidth = width * 0.5;
		height = width; // on init
		halfheight = halfwidth;
		dur = idur;
		serport = iserport;
		offsetheading = ioffsetheading;
		recchans = irecchans;
		recbus = irecbus;
		guiflag = iguiflag;

		currentsource = 0;
		lastGui = Main.elapsedTime;
		guiInt = iguiint;
		autoloopval = iautoloop;

		looping = false;

		if (serport.notNil) {
			hdtrk = true;
			SerialPort.devicePattern = serport;
			// needed in serKeepItUp routine - see below
			trackPort = SerialPort(serport, 115200, crtscts: true);
			//trackarr= [251, 252, 253, 254, nil, nil, nil, nil, nil, nil,
			//	nil, nil, nil, nil, nil, nil, nil, nil, 255];  //protocol
			trackarr= [251, 252, 253, 254, nil, nil, nil, nil, nil, nil, 255];
			//protocol
			trackarr2= trackarr.copy;
			tracki= 0;
			//track2arr=
			//[247, 248, 249, 250, nil, nil, nil, nil, nil, nil, nil, nil, 255];
			//protocol
			//track2arr2= trackarr.copy;
			//track2i= 0;


			trackPort.doneAction = {
				"Serial port down".postln;
				troutine.stop;
				troutine.reset;
			};


			troutine = Routine.new({
				inf.do{
					this.matchTByte(trackPort.read);
				};
			});

			kroutine = Routine.new({
				inf.do{
					if (trackPort.isOpen.not) // if serial port is closed
					{
						"Trying to reopen serial port!".postln;
						if (SerialPort.devices.includesEqual(serport))
						// and if device is actually connected
						{
							"Device connected! Opening port!".postln;
							troutine.stop;
							troutine.reset;
							trackPort = SerialPort(serport, 115200,
								crtscts: true);
							troutine.play; // start tracker routine again
						}
					};
					1.wait;
				};
			});

			//headingOffset = offsetheading;
		};


		///////////////////// DECLARATIONS FROM gui /////////////////////


		espacializador = Array.newClear(nfontes);
		libName = Array.newClear(nfontes);
		lib = Array.newClear(nfontes);
		dstrv = Array.newClear(nfontes);
		convert = Array.newClear(nfontes);
		lp = Array.newClear(nfontes);
		sp = Array.newClear(nfontes);
		df = Array.newClear(nfontes);
		dstrvtypes = Array.newClear(nfontes);
		hwn = Array.newClear(nfontes);
		scn = Array.newClear(nfontes);
		mbus = Array.newClear(nfontes);
		sbus = Array.newClear(nfontes);
		ncanais = Array.newClear(nfontes);
		// 0 = não, nem estéreo. 1 = mono. 2 = estéreo.
		ncan = Array.newClear(nfontes);
		// 0 = não, nem estéreo. 1 = mono. 2 = estéreo.
		// note that ncan refers to # of channels in streamed sources.
		// ncanais is related to sources read from file
		busini = Array.newClear(nfontes);
		// initial bus # in streamed audio grouping
		// (ie. mono, stereo or b-format)
		aux1 = Array.newClear(nfontes);
		aux2 = Array.newClear(nfontes);
		aux3 = Array.newClear(nfontes);
		aux4 = Array.newClear(nfontes);
		aux5 = Array.newClear(nfontes);

		a1but = Array.newClear(nfontes);
		a2but = Array.newClear(nfontes);
		a3but = Array.newClear(nfontes);
		a4but = Array.newClear(nfontes);
		a5but = Array.newClear(nfontes);

		sombuf = Array.newClear(nfontes);
		//		xoffset = Array.fill(nfontes, 0);
		//		yoffset = Array.fill(nfontes, 0);
		synt = Array.newClear(nfontes);
		//sprite = Array2D.new(nfontes, 2);
		angle = Array.newClear(nfontes); // ângulo dos canais estereofônicos
		zlev = Array.newClear(nfontes);
		level = Array.newClear(nfontes);
		//	doplev = Array.newClear(nfontes);
		glev = Array.newClear(nfontes);
		llev = Array.newClear(nfontes);
		rm = Array.newClear(nfontes);
		dm = Array.newClear(nfontes);
		rlev = Array.newClear(nfontes);
		dlev = Array.newClear(nfontes);
		dplev = Array.newClear(nfontes);
		clev = Array.newClear(nfontes);
		grainrate = Array.newClear(nfontes);
		winsize = Array.newClear(nfontes);
		winrand = Array.newClear(nfontes);

		ncanbox = Array.newClear(nfontes);
		businibox = Array.newClear(nfontes);
		playingBF = Array.newClear(nfontes);


		//oxbox = Array.newClear(nfontes);
		//oybox = Array.newClear(nfontes);
		//ozbox = Array.newClear(nfontes);
		xbox = Array.newClear(nfontes);
		zbox = Array.newClear(nfontes);
		ybox = Array.newClear(nfontes);
		abox = Array.newClear(nfontes); // ângulo
		vbox = Array.newClear(nfontes);  // level
		gbox = Array.newClear(nfontes); // reverberação global
		lbox = Array.newClear(nfontes); // reverberação local
		rmbox = Array.newClear(nfontes); // local room size
		dmbox = Array.newClear(nfontes); // local dampening
		rbox = Array.newClear(nfontes); // rotação de b-format
		dbox = Array.newClear(nfontes); // diretividade de b-format
		cbox = Array.newClear(nfontes); // contrair b-format
		dpbox = Array.newClear(nfontes); // dop amount
		libbox = Array.newClear(nfontes); // libs
		lpcheck = Array.newClear(nfontes); // loop
		spcheck = Array.newClear(nfontes); // spread
		dfcheck = Array.newClear(nfontes); // diffuse
		dstrvbox = Array.newClear(nfontes); // distant reverb list
		ratebox = Array.newClear(nfontes); // grain rate
		winbox = Array.newClear(nfontes); // granular window size
		randbox = Array.newClear(nfontes); // granular randomize window
		hwncheck = Array.newClear(nfontes); // hardware-in check
		scncheck = Array.newClear(nfontes); // SuperCollider-in check
		a1box = Array.newClear(nfontes); // aux - array of num boxes in data window
		a2box = Array.newClear(nfontes); // aux - array of num boxes in data window
		a3box = Array.newClear(nfontes); // aux - array of num boxes in data window
		a4box = Array.newClear(nfontes); // aux - array of num boxes in data window
		a5box = Array.newClear(nfontes); // aux - array of num boxes in data window

		a1but = Array.newClear(nfontes); // aux - array of buttons in data window
		a2but = Array.newClear(nfontes); // aux - array of buttons in data window
		a3but = Array.newClear(nfontes); // aux - array of buttons in data window
		a4but = Array.newClear(nfontes); // aux - array of buttons in data window
		a5but = Array.newClear(nfontes); // aux - array of buttons in data window

		a1check = Array.newClear(nfontes); // aux - array of buttons in data window
		a2check = Array.newClear(nfontes); // aux - array of buttons in data window
		a3check = Array.newClear(nfontes); // aux - array of buttons in data window
		a4check = Array.newClear(nfontes); // aux - array of buttons in data window
		a5check = Array.newClear(nfontes); // aux - array of buttons in data window

		stcheck = Array.newClear(nfontes); // aux - array of buttons in data window

		firstTime = Array.newClear(nfontes);


		tfield = Array.newClear(nfontes);
		streamdisk = Array.newClear(nfontes);

		// busses to send audio from player to spatialiser synths
		nfontes.do { | x |
			mbus[x] = Bus.audio(server, 1);
			sbus[x] = Bus.audio(server, 2);
			//	bfbus[x] = Bus.audio(s, 4);
		};


		audit = Array.newClear(nfontes);

		origine = Cartesian();

		pitch = 0;
		roll = 0;
		heading = 0;

		clsRvtypes = ""; // initialise close reverb type
		clsrv = 0;
		clsrm = 0.5; // initialise close reverb room size
		clsdm = 0.5; // initialise close reverb dampening


		////////////////////////////////////////////////

		////////// ADDED NEW ARRAYS and other proxy stuff  //////////////////

		// these proxies behave like GUI elements. They eneable
		// the use of Automation without a GUI

		cartval = Array.fill(nfontes, {Cartesian(0, 200, 0)});
		spheval = Array.fill(nfontes, {|i| cartval[i].asSpherical});

		rboxProxy = Array.newClear(nfontes);
		cboxProxy = Array.newClear(nfontes);
		aboxProxy = Array.newClear(nfontes);
		vboxProxy = Array.newClear(nfontes);
		gboxProxy = Array.newClear(nfontes);
		lboxProxy = Array.newClear(nfontes);
		rmboxProxy = Array.newClear(nfontes);
		dmboxProxy = Array.newClear(nfontes);
		dboxProxy = Array.newClear(nfontes);
		dpboxProxy = Array.newClear(nfontes);
		zboxProxy = Array.newClear(nfontes);
		yboxProxy = Array.newClear(nfontes);
		xboxProxy = Array.newClear(nfontes);
		a1checkProxy = Array.newClear(nfontes);
		a2checkProxy = Array.newClear(nfontes);
		a3checkProxy = Array.newClear(nfontes);
		a4checkProxy = Array.newClear(nfontes);
		a5checkProxy = Array.newClear(nfontes);
		a1boxProxy = Array.newClear(nfontes);
		a2boxProxy = Array.newClear(nfontes);
		a3boxProxy = Array.newClear(nfontes);
		a4boxProxy = Array.newClear(nfontes);
		a5boxProxy = Array.newClear(nfontes);

		tfieldProxy = Array.newClear(nfontes);
		libboxProxy = Array.newClear(nfontes);
		lpcheckProxy = Array.newClear(nfontes);
		dstrvboxProxy = Array.newClear(nfontes);
		hwncheckProxy = Array.newClear(nfontes);
		scncheckProxy = Array.newClear(nfontes);
		dfcheckProxy = Array.newClear(nfontes);
		spcheckProxy = Array.newClear(nfontes);
		ncanboxProxy = Array.newClear(nfontes);
		businiboxProxy = Array.newClear(nfontes);
		stcheckProxy = Array.newClear(nfontes);
		rateboxProxy = Array.newClear(nfontes);
		winboxProxy = Array.newClear(nfontes);
		randboxProxy = Array.newClear(nfontes);

		//set up automationProxy for single parameters outside of the previous loop,
		// not to be docked
		masterlevProxy = AutomationGuiProxy(1);
		clsrvboxProxy = AutomationGuiProxy(0);
		clsrmboxProxy = AutomationGuiProxy(0.5); // cls roomsize proxy
		clsdmboxProxy = AutomationGuiProxy(0.5); // cls dampening proxy

		oxnumboxProxy = AutomationGuiProxy(0.0);
		oynumboxProxy = AutomationGuiProxy(0.0);
		oznumboxProxy = AutomationGuiProxy(0.0);

		pitchnumboxProxy = AutomationGuiProxy(0.0);
		rollnumboxProxy = AutomationGuiProxy(0.0);
		headingnumboxProxy = AutomationGuiProxy(0.0);

		control = Automation(dur, showLoadSave: false, showSnapshot: true,
			minTimeStep: 0.001);


		////////////// DOCK PROXIES /////////////


		// this should be done after the actions are assigned


		nfontes.do { | i |

			libName[i] = spatList[0];
			lib[i] = 0;
			dstrv[i] = 0;
			convert[i] = false;
			angle[i] = 1.05;
			level[i] = 1;
			glev[i] = 0;
			llev[i] = 0;
			rm[i] = 0.5;
			dm[i] = 0.5;
			lp[i] = 0;
			sp[i] = 0;
			df[i] = 0;
			dstrvtypes[i] = ""; // initialise distants reverbs types
			hwn[i] = 0;
			scn[i] = 0;
			rlev[i] = 0;
			dlev[i] = 0;
			clev[i] = 1;
			zlev[i] = 0;
			dplev[i] = 0;
			grainrate[i] = 10;
			winsize[i] = 0.1;
			winrand[i] = 0;

			aux1[i] = 0;
			aux2[i] = 0;
			aux3[i] = 0;
			aux4[i] = 0;
			aux5[i] = 0;
			streamdisk[i] = false;
			ncan[i] = 0;
			ncanais[i] = 0;
			busini[i] = 0;
			audit[i] = false;
			playingBF[i] = false;
			firstTime[i] = true;

			rboxProxy[i] = AutomationGuiProxy(0.0);
			cboxProxy[i] = AutomationGuiProxy(0.0);
			aboxProxy[i] = AutomationGuiProxy(1.0471975511966);
			vboxProxy[i] = AutomationGuiProxy(1.0);
			gboxProxy[i] = AutomationGuiProxy(0.0);
			lboxProxy[i] = AutomationGuiProxy(0.0);
			rmboxProxy[i]= AutomationGuiProxy(0.5);
			dmboxProxy[i]= AutomationGuiProxy(0.5);
			dboxProxy[i] = AutomationGuiProxy(0.0);
			dpboxProxy[i] = AutomationGuiProxy(0.0);
			zboxProxy[i] = AutomationGuiProxy(0.0);
			yboxProxy[i] = AutomationGuiProxy(200.0);
			xboxProxy[i] = AutomationGuiProxy(0.0);
			a1checkProxy[i] = AutomationGuiProxy(false);
			a2checkProxy[i] = AutomationGuiProxy(false);
			a3checkProxy[i] = AutomationGuiProxy(false);
			a4checkProxy[i] = AutomationGuiProxy(false);
			a5checkProxy[i] = AutomationGuiProxy(false);
			a1boxProxy[i] = AutomationGuiProxy(0.0);
			a2boxProxy[i] = AutomationGuiProxy(0.0);
			a3boxProxy[i] = AutomationGuiProxy(0.0);
			a4boxProxy[i] = AutomationGuiProxy(0.0);
			a5boxProxy[i] = AutomationGuiProxy(0.0);

			hwncheckProxy[i] = AutomationGuiProxy(false);

			tfieldProxy[i] = AutomationGuiProxy("");
			libboxProxy[i] = AutomationGuiProxy(lib[i]);
			lpcheckProxy[i] = AutomationGuiProxy(false);
			dstrvboxProxy[i] = AutomationGuiProxy(0);
			scncheckProxy[i] = AutomationGuiProxy(false);
			dfcheckProxy[i] = AutomationGuiProxy(false);
			spcheckProxy[i] = AutomationGuiProxy(false);
			ncanboxProxy[i] = AutomationGuiProxy(0);
			businiboxProxy[i] = AutomationGuiProxy(0);
			stcheckProxy[i] = AutomationGuiProxy(false);
			rateboxProxy[i] = AutomationGuiProxy(10.0);
			winboxProxy[i] = AutomationGuiProxy(0.1);
			randboxProxy[i] = AutomationGuiProxy(0);


			libboxProxy[i].action_({ | num |

				libName[i] = spatList[num.value];

				if (guiflag) {
					{ libbox[i].value = num.value }.defer;
					if(i == currentsource) {
						{ updateGuiCtl.value(\lib, num.value) }.defer;
					};
				};

				if (ossialib[i].v != num.value) {
					ossialib[i].v_(num.value);
				};
			});


			dstrvboxProxy[i].action_({ | num |
				case
				{ num.value == 0 }
				{ dstrvtypes[i] = "";
					this.setSynths(i, \rv, 0); }
				{ num.value == 1 }
				{ dstrvtypes[i] = "_free";
					this.setSynths(i, \rv, 0); }
				{ num.value == 2 }
				{ dstrvtypes[i] = "_pass";
					this.setSynths(i, \rv, 0); }
				{ num.value >= 3 }
				{ dstrvtypes[i] = "_conv";
					this.setSynths(i, \rv, 0); };

				if (guiflag) {
					{dstrvbox[i].value = num.value}.defer;

					if (i == currentsource) {
						{ updateGuiCtl.value(\dstrv, num.value); }.defer;
						if (num.value == 3) {
							this.setSynths(i, \rv, 1);
						}{
							this.setSynths(i, \rv, 0);
						};
					};
				};

				if (ossiadst[i].v != num.value) {
					ossiadst[i].v_(num.value);
				};
			});


			xboxProxy[i].action = { | num |
				var sphe, sphediff;
				cartval[i].x_(num.value);
				sphe = (cartval[i] - origine).rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				sphediff = [sphe.rho, (sphe.theta - halfPi).wrap(-pi, pi), sphe.phi];
				if (ossiaSpheBack && (ossiasphe[i].v != sphediff)) {
					ossiaCartBack = false;
					ossiasphe[i].v_(sphediff);
					ossiaCartBack = true;
				};
				if (ossiacart[i].v[0] != num.value) {
					ossiacart[i].v_([num.value, yboxProxy[i].value,
						zboxProxy[i].value]);
				};

				if (guiflag) {
					{xbox[i].value = num.value}.defer;
					{novoplot.value;}.defer;
				};
			};

			yboxProxy[i].action = { | num |
				var sphe, sphediff;
				cartval[i].y_(num.value);
				sphe = (cartval[i] - origine).rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				sphediff = [sphe.rho, (sphe.theta - halfPi).wrap(-pi, pi), sphe.phi];
				if (ossiaSpheBack && (ossiasphe[i].v != sphediff)) {
					ossiaCartBack = false;
					ossiasphe[i].v_(sphediff);
					ossiaCartBack = true;
				};
				if (ossiacart[i].v[1] != num.value) {
					ossiacart[i].v_([xboxProxy[i].value, num.value,
						zboxProxy[i].value]);
				};

				if (guiflag) {
					{ybox[i].value = num.value}.defer;
					{novoplot.value;}.defer;
				};
			};

			zboxProxy[i].action = { | num |
				var sphe, sphediff;
				cartval[i].z_(num.value);
				sphe = (cartval[i] - origine).rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				sphediff = [sphe.rho, (sphe.theta - halfPi).wrap(-pi, pi), sphe.phi];
				if (ossiaSpheBack && (ossiasphe[i].v != sphediff)) {
					ossiaCartBack = false;
					ossiasphe[i].v_(sphediff);
					ossiaCartBack = true;
				};
				if (ossiacart[i].v[2] != num.value) {
					ossiacart[i].v_([xboxProxy[i].value, yboxProxy[i].value,
						num.value]);
				};
				zlev[i] = spheval[i].z;
				if (guiflag) {
					{zbox[i].value = num.value}.defer;
					{novoplot.value;}.defer;
				};
			};

			aboxProxy[i].action = { | num |
				if(espacializador[i].notNil) {
					angle[i] = num.value;
					espacializador[i].set(\angle, num.value);
					this.setSynths(i, \angle, num.value);
					angle[i] = num.value;
				};
				if (guiflag) {
					{abox[i].value = num.value}.defer;
					if(i == currentsource)
					{
						{winCtl[0][7].value = num.value}.defer;
						{winCtl[1][7].value = num.value / pi}.defer;
					};
				};

				if (ossiaangle[i].v != num.value) {
					ossiaangle[i].v_(num.value);
				};
			};

			vboxProxy[i].action = { | num |
				espacializador[i].set(\level, num.value);
				this.setSynths(i, \level, num.value);
				level[i] = num.value;

				if (guiflag) {
					{ vbox[i].value = num.value }.defer;
					if(i == currentsource)
					{
						{ winCtl[1][0].value = num.value * 0.5 }.defer;
						{ winCtl[0][0].value = num.value }.defer;
					};
				};

				if (ossialev[i].v != num.value) {
					ossialev[i].v_(num.value);
				};
			};

			gboxProxy[i].action = { | num |
				espacializador[i].set(\glev, num.value);
				this.setSynths(i, \glev, num.value);
				synt[i].set(\glev, num.value);
				glev[i] = num.value;
				if (guiflag) {
					{ gbox[i].value = num.value }.defer;
					if (i == currentsource)
					{
						{ winCtl[0][3].value = num.value }.defer;
						{ winCtl[1][3].value = num.value }.defer;
					};
				};

				if (ossiaclsam[i].v != num.value) {
					ossiaclsam[i].v_(num.value);
				};
			};

			lboxProxy[i].action = { | num |
				espacializador[i].set(\llev, num.value);
				this.setSynths(i, \llev, num.value);
				synt[i].set(\llev, num.value);
				llev[i] = num.value;
				if (guiflag) {
					{ lbox[i].value = num.value; }.defer;
					if (i == currentsource)
					{
						{ winCtl[0][4].value = num.value }.defer;
						{ winCtl[1][4].value = num.value }.defer;
					};
				};

				if (ossiadstam[i].v != num.value) {
					ossiadstam[i].v_(num.value);
				};
			};

			rmboxProxy[i].action = { | num |
				espacializador[i].set(\room, num.value);
				this.setSynths(i, \room, num.value);
				synt[i].set(\room, num.value);
				rm[i] = num.value;
				if (guiflag) {
					{ rmbox[i].value = num.value; }.defer;
					if (i == currentsource) {
						{ winCtl[0][5].value = num.value }.defer;
						{ winCtl[1][5].value = num.value }.defer;
					};
				};

				if (ossiadstdel[i].v != num.value) {
					ossiadstdel[i].v_(num.value);
				};
			};

			dmboxProxy[i].action = { | num |
				espacializador[i].set(\damp, num.value);
				this.setSynths(i, \damp, num.value);
				synt[i].set(\damp, num.value);
				dm[i] = num.value;
				if (guiflag) {
					{ dmbox[i].value = num.value; };
					if (i == currentsource) {
						{ winCtl[0][6].value = num.value }.defer;
						{ winCtl[1][6].value = num.value }.defer;
					};
				};

				if (ossiadstdec[i].v != num.value) {
					ossiadstdec[i].v_(num.value);
				};
			};

			rboxProxy[i].action = { | num |
				synt[i].set(\rotAngle, num.value);
				this.setSynths(i, \rotAngle, num.value);
				rlev[i] = num.value;
				if (guiflag) {
					{rbox[i].value = num.value}.defer;
					if(i == currentsource)
					{
						{winCtl[0][8].value = num.value}.defer;
						{winCtl[1][8].value = (num.value + pi) / 2pi}.defer;
					};
				};

				if (ossiarot[i].v != num.value) {
					ossiarot[i].v_(num.value);
				};
			};

			dboxProxy[i].action = { | num |
				synt[i].set(\directang, num.value);
				this.setSynths(i, \directang, num.value);
				dlev[i] = num.value;
				if (guiflag) {
					{dbox[i].value = num.value}.defer;
					if(i == currentsource)
					{
						{winCtl[0][9].value = num.value}.defer;
						{winCtl[1][9].value = num.value / halfPi}.defer;
					};
				};

				if (ossiadir[i].v != num.value) {
					ossiadir[i].v_(num.value);
				};
			};

			cboxProxy[i].action = { | num |
				synt[i].set(\contr, num.value);
				// TESTING
				espacializador[i].set(\contr, num.value);
				this.setSynths(i, \contr, num.value);
				clev[i] = num.value;
				if (guiflag) {
					{cbox[i].value = num.value}.defer;
					if (i == currentsource)
					{
						{winCtl[0][2].value = num.value}.defer;
						{winCtl[1][2].value = num.value}.defer;
					};
				};

				if (ossiactr[i].v != num.value) {
					ossiactr[i].v_(num.value);
				};
			};

			dpboxProxy[i].action = { | num |
				// used for b-format amb/bin only
				synt[i].set(\dopamnt, num.value);
				this.setSynths(i, \dopamnt, num.value);
				// used for the others
				espacializador[i].set(\dopamnt, num.value);
				dplev[i] = num.value;
				if (guiflag) {
					{dpbox[i].value = num.value}.defer;
					if(i == currentsource) {
						{winCtl[1][1].value = num.value}.defer;
						{winCtl[0][1].value = num.value}.defer;
					};
				};

				if (ossiadp[i].v != num.value) {
					ossiadp[i].v_(num.value);
				};
			};


			a1boxProxy[i].action = { | num |
				this.setSynths(i, \aux1, num.value);
				aux1[i] = num.value;
				if((i == currentsource) && guiflag)
				{
					{auxslider1.value = num.value}.defer;
					{aux1numbox.value = num.value}.defer;
				};
				if (guiflag) {
					{a1box[i].value = num.value}.defer;
				};
			};

			a2boxProxy[i].action = { | num |
				this.setSynths(i, \aux2, num.value);
				aux2[i] = num.value;
				if((i == currentsource) && guiflag)
				{
					{auxslider2.value = num.value}.defer;
					{aux2numbox.value = num.value}.defer;
				};
				if (guiflag) {
					{a2box[i].value = num.value}.defer;
				};
			};

			a3boxProxy[i].action = { | num |
				this.setSynths(i, \aux3, num.value);
				aux3[i] = num.value;
				if((i == currentsource) && guiflag)
				{
					{auxslider3.value = num.value}.defer;
					{aux3numbox.value = num.value}.defer;
				};
				if (guiflag) {
					{a3box[i].value = num.value}.defer;
				};
			};

			a4boxProxy[i].action = { | num |
				this.setSynths(i, \aux4, num.value);
				aux4[i] = num.value;
				if((i == currentsource) && guiflag)
				{
					{auxslider4.value = num.value}.defer;
					{aux4numbox.value = num.value}.defer;
				};
				if (guiflag) {
					{a4box[i].value = num.value}.defer;
				};
			};

			a5boxProxy[i].action = { | num |
				this.setSynths(i, \aux5, num.value);
				aux5[i] = num.value;
				if((i == currentsource) && guiflag)
				{
					{auxslider5.value = num.value}.defer;
					{aux5numbox.value = num.value}.defer;
				};
				if (guiflag) {
					{a5box[i].value = num.value}.defer;
				};
			};

			a1checkProxy[i].action = { | but |

				if (but.value) {
					a1but[i] = 1;
					this.setSynths(i, \a1check, 1);
				}{
					this.a1but[i] = 0;
					setSynths(i, \a1check, 0);
				};

				if (guiflag) {

					{a1check[i].value = but.value}.defer;
				};
			};

			a2checkProxy[i].action = { | but |

				if (but.value) {
					a2but[i] = 1;
					this.setSynths(i, \a2check, 1);
				}{
					a2but[i] = 0;
					this.setSynths(i, \a2check, 0);
				};
				if (guiflag) {
					{a2check[i].value = but.value}.defer;
				};
			};


			a3checkProxy[i].action = { | but |

				if (but.value) {
					a3but[i] = 1;
					this.setSynths(i, \a3check, 1);
				}{
					a3but[i] = 0;
					this.setSynths(i, \a3check, 0);
				};
				if (guiflag) {
					{a3check[i].value = but.value}.defer;
				};
			};

			a4checkProxy[i].action = { | but |

				if (but.value) {
					a4but[i] = 1;
					this.setSynths(i, \a4check, 1);
				}{
					a4but[i] = 0;
					this.setSynths(i, \a4check, 0);
				};
				if (guiflag) {
					{a4check[i].value = but.value}.defer;
				};
			};

			a5checkProxy[i].action = { | but |

				if (but.value) {
					a5but[i] = 1;
					this.setSynths(i, \a5check, 1);
				}{
					a5but[i] = 0;
					this.setSynths(i, \a5check, 0);
				};
				if (guiflag) {
					{a5check[i].value = but.value}.defer;
				};
			};

			stcheckProxy[i].action = { | but |
				if (but.value) {
					streamdisk[i] = true;
				}{
					streamdisk[i] = false;
				};
				if (guiflag) {
					{stcheck[i].value = but.value}.defer;
				};
			};


			lpcheckProxy[i].action_({ | but |
				if (but.value) {
					lp[i] = 1;
					synt[i].set(\lp, 1);
					this.setSynths(i, \lp, 1);
				} {
					lp[i] = 0;
					synt[i].set(\lp, 0);
					this.setSynths(i, \lp, 0);
				};
				if (guiflag) {
					{ lpcheck[i].value = but.value }.defer;
					if(i==currentsource) {
						{ loopcheck.value = but.value }.defer;
					};
				};

				if (ossialoop[i].v != but.value.asBoolean) {
					ossialoop[i].v_(but.value.asBoolean);
				};
			});

			hwncheckProxy[i].action = { | but |
				if((i==currentsource) && guiflag) {{hwInCheck.value = but.value}.defer;
				};
				if (but.value == true) {
					if (guiflag) {
						{scncheck[i].value = false}.defer;
					};
					if((i==currentsource) && guiflag){{scInCheck.value = false}.defer;};
					hwn[i] = 1;
					scn[i] = 0;
					synt[i].set(\hwn, 1);
				}{
					hwn[i] = 0;
					synt[i].set(\hwn, 0);
				};
				if (guiflag) {
					{hwncheck[i].value = but.value}.defer;
					updateGuiCtl.value(\src);
				};
			};

			scncheckProxy[i].action_({ | but |
				if((i==currentsource) && guiflag) {{scInCheck.value = but.value}.defer;};
				if (but.value == true) {
					if (guiflag) {
						{hwncheck[i].value = false}.defer;
					};
					if((i==currentsource) && guiflag){{hwInCheck.value = false}.defer;};
					scn[i] = 1;
					hwn[i] = 0;
					synt[i].set(\scn, 1);
				}{
					scn[i] = 0;
					synt[i].set(\scn, 0);
				};
				if (guiflag) {
					{scncheck[i].value = but.value}.defer;
					updateGuiCtl.value(\src);
				};
			});

			spcheckProxy[i].action_({ | but |
				if((i==currentsource) && guiflag){{spreadcheck.value = but.value}.defer;};
				if (but.value) {
					if (guiflag) {
						{dfcheck[i].value = false}.defer;
					};
					if((i==currentsource) && guiflag){
						{diffusecheck.value = false}.defer;};
					sp[i] = 1;
					df[i] = 0;
					espacializador[i].set(\sp, 1);
					espacializador[i].set(\df, 0);
					synt[i].set(\sp, 1);
					this.setSynths(i, \ls, 1);
					ossiadiff[i].v_(false);
				} {
					sp[i] = 0;
					espacializador[i].set(\sp, 0);
					synt[i].set(\sp, 0);
					this.setSynths(i, \sp, 0);
				};
				if (guiflag) {
					{spcheck[i].value = but.value}.defer;
				};

				if (ossiaspread[i].v != but.value.asBoolean) {
					ossiaspread[i].v_(but.value.asBoolean);
				};
			});

			dfcheckProxy[i].action_({ | but |
				if((i==currentsource) && guiflag){
					{diffusecheck.value = but.value}.defer;};
				if (but.value) {
					if (guiflag) {
						{spcheck[i].value = false}.defer;
					};
					if((i==currentsource) && guiflag) {
						{spreadcheck.value = false}.defer;
					};
					df[i] = 1;
					sp[i] = 0;
					espacializador[i].set(\df, 1);
					espacializador[i].set(\sp, 0);
					synt[i].set(\df, 1);
					this.setSynths(i, \df, 1);
					ossiaspread[i].v_(false);
				} {
					df[i] = 0;
					espacializador[i].set(\df, 0);
					synt[i].set(\df, 0);
					this.setSynths(i, \df, 0);
				};
				if (guiflag) {
					{dfcheck[i].value = but.value}.defer;
				};

				if (ossiadiff[i].v != but.value.asBoolean) {
					ossiadiff[i].v_(but.value.asBoolean);
				};
			});

			ncanboxProxy[i].action = { | num |
				ncan[i] = num.value;
				if (guiflag ) {
					{ ncanbox[i].value = num.value }.defer;

					if (i == currentsource) {
						{ updateGuiCtl.value(\chan); }.defer;
					};
				};
			};

			businiboxProxy[i].action = { | num |
				busini[i] = num.value;
				if (guiflag) {
					{ businibox[i].value = num.value }.defer;

					if (i == currentsource) {
						{ updateGuiCtl.value(\src); }.defer;
					};
				};
			};

			rateboxProxy[i].action = { | num |
				espacializador[i].set(\grainrate, num.value);
				this.setSynths(i, \grainrate, num.value);
				synt[i].set(\grainrate, num.value);
				grainrate[i] = num.value;
				if (guiflag) {
					{ ratebox[i].value = num.value }.defer;
					if (i == currentsource) {
						{ winCtl[0][10].value = num.value }.defer;
						{ winCtl[1][10].value = (num.value - 1) / 59 }.defer;
					};
				};

				if (ossiarate[i].v != num.value) {
					ossiarate[i].v_(num.value);
				};
			};

			winboxProxy[i].action = { | num |
				espacializador[i].set(\winsize, num.value);
				this.setSynths(i, \winsize, num.value);
				synt[i].set(\winsize, num.value);
				winsize[i] = num.value;
				if (guiflag) {
					{ winbox[i].value = num.value; }.defer;
					if (i == currentsource) {
						{ winCtl[0][11].value = num.value }.defer;
						{ winCtl[1][11].value = num.value * 5 }.defer;
					};
				};

				if (ossiawin[i].v != num.value) {
					ossiawin[i].v_(num.value);
				};
			};

			randboxProxy[i].action = { | num |
				espacializador[i].set(\winrand, num.value);
				this.setSynths(i, \winrand, num.value);
				synt[i].set(\winrand, num.value);
				winrand[i] = num.value;
				if (guiflag) {
					{ randbox[i].value = num.value; }.defer;
					if (i == currentsource) {
						{winCtl[0][12].value = num.value}.defer;
						{winCtl[1][12].value = num.value.sqrt}.defer;
					};
				};

				if (ossiarand[i].v != num.value) {
					ossiarand[i].v_(num.value);
				};
			};

			tfieldProxy[i].action = { | path |

				if (path != "") {
					var sf = SoundFile.new;
					sf.openRead(path);
					ncanais[i] = sf.numChannels;
					sf.close;

					if (streamdisk[i].not) {
						if (sombuf[i].notNil) {
							sombuf[i].freeMsg({
								"Buffer freed".postln;
							});
						};

						sombuf[i] = Buffer.read(server, path.value, action: { | buf |
							"Loaded file".postln;
						});
					} {
						"To stream file".postln;
					};
				} {
					if (sombuf[i].notNil) {
						sombuf[i].freeMsg({
							sombuf[i] = nil;
							"Buffer freed".postln;
						});
					};

					ncanais[i] = 0;
				};

				if (guiflag) {
					{ tfield[i].value = path.value; }.defer;
					{ updateGuiCtl.value(\chan); }.defer;
				};

				ossiaaud[i].description = PathName(path.value).fileNameWithoutExtension;
			};

			control.dock(xboxProxy[i], "x_axisProxy_" ++ i);
			control.dock(yboxProxy[i], "y_axisProxy_" ++ i);
			control.dock(zboxProxy[i], "z_axisProxy_" ++ i);
			control.dock(vboxProxy[i], "levelProxy_" ++ i);
			control.dock(dpboxProxy[i], "dopamtProxy_" ++ i);
			control.dock(gboxProxy[i], "revglobalProxy_" ++ i);
			control.dock(dstrvboxProxy[i], "localrevkindProxy_" ++ i);
			control.dock(lboxProxy[i], "revlocalProxy_" ++ i);
			control.dock(rmboxProxy[i], "localroomProxy_" ++ i);
			control.dock(dmboxProxy[i], "localdampProxy_" ++ i);
			control.dock(aboxProxy[i], "angleProxy_" ++ i);
			control.dock(rboxProxy[i], "rotationProxy_" ++ i);
			control.dock(dboxProxy[i], "directivityProxy_" ++ i);
			control.dock(cboxProxy[i], "contractionProxy_" ++ i);
			control.dock(rateboxProxy[i], "grainrateProxy_" ++ i);
			control.dock(winboxProxy[i], "windowsizeProxy_" ++ i);
			control.dock(randboxProxy[i], "randomwindowProxy_" ++ i);
			control.dock(a1boxProxy[i], "aux1Proxy_" ++ i);
			control.dock(a2boxProxy[i], "aux2Proxy_" ++ i);
			control.dock(a3boxProxy[i], "aux3Proxy_" ++ i);
			control.dock(a4boxProxy[i], "aux4Proxy_" ++ i);
			control.dock(a5boxProxy[i], "aux5Proxy_" ++ i);
			control.dock(a1checkProxy[i], "aux1checkProxy_" ++ i);
			control.dock(a2checkProxy[i], "aux2checkProxy_" ++ i);
			control.dock(a3checkProxy[i], "aux3checkProxy_" ++ i);
			control.dock(a4checkProxy[i], "aux4checkProxy_" ++ i);
			control.dock(a5checkProxy[i], "aux5checkProxy_" ++ i);
			//control.dock(stcheckProxy[i], "stcheckProxy_" ++ i);

		};


		masterlevProxy.action_({ | num |

			globDec.set(\level, num.value);

			if (guiflag) {
				masterslider.value = num.value * 0.5;
			};

			if (ossiamaster.v != num.value) {
				ossiamaster.v_(num.value);
			};
		});


		clsrvboxProxy.action_({ | num |

			clsrv = num.value;

			case
			{ num.value == 1 }
			{ clsRvtypes = "_free"; }
			{ num.value == 2 }
			{ clsRvtypes = "_pass"; }
			{ num.value > 2 }
			{ clsRvtypes = "_conv"; };

			if (num.value == 0)
			{
				if (revGlobal.notNil)
				{ revGlobal.set(\gate, 0) };
			} {
				if (convert_fuma) {
					if (convertor.notNil) {
						convertor.set(\gate, 1);
					} {
						convertor = Synth(\ambiConverter, [\gate, 1],
							target:glbRevDecGrp).onFree({
							convertor = nil;
						});
					};
				};

				if (revGlobal.notNil)
				{ revGlobal.set(\gate, 0); };

				revGlobal = Synth(\revGlobalAmb++clsRvtypes,
					[\gate, 1, \room, clsrm, \damp, clsdm] ++
					irSpecPar.value(max((clsrv - 3), 0)),
					glbRevDecGrp).register.onFree({
					if (revGlobal.isPlaying.not) {
						revGlobal = nil;
					};
					if (convertor.notNil) {
						if (convert_fuma) {
							if (this.converterNeeded(0).not) {
								convertor.set(\gate, 0);
							};
						};
					};
				});

			};

			if (guiflag) {
				{ updateGuiCtl.value(\clsrv, num.value) }.defer;
			};

			if (ossiacls.v != num.value) {
				ossiacls.v_(num.value);
			};
		});


		clsrmboxProxy.action_({ | num |
			revGlobal.set(\room, num.value);
			clsrm = num.value;

			if (guiflag) {
				{originCtl[1][0].value = num.value}.defer;
			};

			if (ossiaclsdel.v != num.value) {
				ossiaclsdel.v_(num.value);
			};
		});


		clsdmboxProxy.action_({ | num |

			revGlobal.set(\damp, num.value);
			clsdm = num.value;

			if (guiflag) {
				{originCtl[1][1].value = num.value}.defer;
			};

			if (ossiaclsdec.v != num.value) {
				ossiaclsdec.v_(num.value);
			};
		});


		oxnumboxProxy.action_({ | num |

			ossiaorigine.v_([num.value, oynumboxProxy.value,
				oznumboxProxy.value]);

			origine.x_(num.value);

			ossiaCartBack = false;

			nfontes.do {  | i |
				var cart = (cartval[i] - origine)
				.rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				ossiasphe[i].v_([cart.rho,
					(cart.theta - halfPi).wrap(-pi, pi), cart.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][5].value = num.value;}.defer;
			};
		});



		oynumboxProxy.action_({ | num |

			ossiaorigine.v_([oxnumboxProxy.value, num.value,
				oznumboxProxy.value]);

			origine.y_(num.value);

			ossiaCartBack = false;

			nfontes.do { | i |
				var cart = (cartval[i] - origine)
				.rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				ossiasphe[i].v_([cart.rho,
					(cart.theta - halfPi).wrap(-pi, pi), cart.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][6].value = num.value;}.defer;
			};
		});


		oznumboxProxy.action_({ | num |

			ossiaorigine.v_([oxnumboxProxy.value,
				oynumboxProxy.value, num.value]);

			origine.z_(num.value);

			ossiaCartBack = false;

			nfontes.do { | i |
				var cart = (cartval[i] - origine)
				.rotate(heading.neg).tilt(pitch.neg).tumble(roll.neg);

				ossiasphe[i].v_([cart.rho,
					(cart.theta - halfPi).wrap(-pi, pi), cart.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][7].value = num.value;}.defer;
			};
		});



		headingnumboxProxy.action_({ | num |

			ossiaorient.v_([num.value, pitchnumboxProxy.value,
				rollnumboxProxy.value]);
			ossiaCartBack = false;

			nfontes.do { | i |
				var euler = (cartval[i] - origine)
				.rotate(num.value.neg).tilt(pitch.neg).tumble(roll.neg);

				ossiasphe[i].v_([euler.rho,
					(euler.theta - halfPi).wrap(-pi, pi), euler.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;
			heading = num.value;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][2].value = num.value;}.defer;
			};
		});


		pitchnumboxProxy.action_({ | num |

			ossiaorient.v_([headingnumboxProxy.value, num.value,
				rollnumboxProxy.value]);
			ossiaCartBack = false;

			nfontes.do { | i |
				var euler = (cartval[i] - origine)
				.rotate(heading.neg).tilt(num.value.neg).tumble(roll.neg);

				ossiasphe[i].v_([euler.rho,
					(euler.theta - halfPi).wrap(-pi, pi), euler.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;
			pitch = num.value;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][3].value = num.value;}.defer;
			};
		});


		rollnumboxProxy.action_({ | num |

			ossiaorient.v_([headingnumboxProxy.value,
				pitchnumboxProxy.value, num.value]);
			ossiaCartBack = false;

			nfontes.do { | i |
				var euler = (cartval[i] - origine)
				.rotate(heading.neg).tilt(pitch.neg).tumble(num.value.neg);

				ossiasphe[i].v_([euler.rho,
					(euler.theta - halfPi).wrap(-pi, pi), euler.phi]);

				zlev[i] = spheval[i].z;
			};

			ossiaCartBack = true;
			roll = num.value;

			if (guiflag) {
				{novoplot.value;}.defer;
				{originCtl[0][4].value = num.value;}.defer;
			};
		});


		control.dock(clsrvboxProxy, "globrevkindProxy");
		control.dock(clsrmboxProxy, "localroomProxy");
		control.dock(clsdmboxProxy, "localdampProxy");
		control.dock(oxnumboxProxy, "oxProxy");
		control.dock(oynumboxProxy, "oyProxy");
		control.dock(oznumboxProxy, "ozProxy");
		control.dock(pitchnumboxProxy, "pitchProxy");
		control.dock(rollnumboxProxy, "rollProxy");
		control.dock(headingnumboxProxy, "headingProxy");


		///////////////////////////////////////////////////

		streambuf = Array.newClear(nfontes);

		// array of functions, 1 for each source (if defined),
		// that will be launched on Automation's "play"
		triggerFunc = Array.newClear(nfontes);
		//companion to above. Launched by "Stop"
		stopFunc = Array.newClear(nfontes);


		// can place headtracker rotations in these functions
		// don't forget that the synthdefs
		// need to have there values for heading, roll and pitch "set" by serial routine
		// NO - DON'T PUT THIS HERE - Make a global synth with a common input bus


		/////////// START code for 2nd order matrices /////////////////////
		/*
		2nd-order FuMa-MaxN A-format decoder & encoder
		Author: Joseph Anderson
		http://www.ambisonictoolkit.net
		Taken from: https://gist.github.com/joslloand/c70745ef0106afded73e1ea07ff69afc
		*/

		// a-12 decoder matrix
		soa_a12_decoder_matrix = Matrix.with([
			[ 0.11785113, 0.212662702, 0, -0.131432778, -0.0355875819, -0.279508497, 0,
				0.226127124, 0 ],
			[ 0.11785113, 0.131432778, -0.212662702, 0, -0.208333333, 0, 0, -0.139754249,
				-0.279508497 ],
			[ 0.11785113, 0, -0.131432778, 0.212662702, 0.243920915, 0, -0.279508497,
				-0.0863728757, 0 ],
			[ 0.11785113, 0.212662702, 0, 0.131432778, -0.0355875819, 0.279508497, 0,
				0.226127124, 0 ],
			[ 0.11785113, -0.131432778, -0.212662702, 0, -0.208333333, 0, 0, -0.139754249,
				0.279508497 ],
			[ 0.11785113, 0, 0.131432778, -0.212662702, 0.243920915, 0, -0.279508497,
				-0.0863728757, 0 ],
			[ 0.11785113, -0.212662702, 0, -0.131432778, -0.0355875819, 0.279508497, 0,
				0.226127124, 0 ],
			[ 0.11785113, -0.131432778, 0.212662702, 0, -0.208333333, 0, 0, -0.139754249,
				-0.279508497 ],
			[ 0.11785113, 0, 0.131432778, 0.212662702, 0.243920915, 0, 0.279508497,
				-0.0863728757, 0 ],
			[ 0.11785113, -0.212662702, 0, 0.131432778, -0.0355875819, -0.279508497, 0,
				0.226127124, 0 ],
			[ 0.11785113, 0.131432778, 0.212662702, 0, -0.208333333, 0, 0, -0.139754249,
				0.279508497 ],
			[ 0.11785113, 0, -0.131432778, -0.212662702, 0.243920915, 0, 0.279508497,
				-0.0863728757, 0 ],
		]);

		// a-12 encoder matrix
		soa_a12_encoder_matrix = Matrix.with([
			[ 0.707106781, 0.707106781, 0.707106781, 0.707106781, 0.707106781,
				0.707106781,0.707106781,
				0.707106781, 0.707106781, 0.707106781, 0.707106781, 0.707106781 ],
			[ 0.850650808, 0.525731112, 0, 0.850650808, -0.525731112, 0, -0.850650808,
				-0.525731112, 0,
				-0.850650808, 0.525731112, 0 ],
			[ 0, -0.850650808, -0.525731112, 0, -0.850650808, 0.525731112, 0, 0.850650808,
				0.525731112,
				0, 0.850650808, -0.525731112 ],
			[ -0.525731112, 0, 0.850650808, 0.525731112, 0, -0.850650808, -0.525731112, 0,
				0.850650808,
				0.525731112, 0, -0.850650808 ],
			[ -0.0854101966, -0.5, 0.585410197, -0.0854101966, -0.5, 0.585410197,
				-0.0854101966, -0.5,
				0.585410197, -0.0854101966, -0.5, 0.585410197 ],
			[ -0.894427191, 0, 0, 0.894427191, 0, 0, 0.894427191, 0, 0, -0.894427191,
				0, 0 ],
			[ 0, 0, -0.894427191, 0, 0, -0.894427191, 0, 0, 0.894427191, 0, 0,
				0.894427191 ],
			[ 0.723606798, -0.447213596, -0.276393202, 0.723606798, -0.447213596,
				-0.276393202,
				0.723606798, -0.447213596, -0.276393202, 0.723606798, -0.447213596,
				-0.276393202 ],
			[ 0, -0.894427191, 0, 0, 0.894427191, 0, 0, -0.894427191, 0, 0, 0.894427191,
				0 ],
		]);

		/*
		1st-order FuMa-MaxN A-format decoder
		*/

		cart = [
			0.850650808352E+00,
			0,
			-0.525731112119E+00,
			0.525731112119E+00,
			-0.850650808352E+00,
			0.000000000000E+00,
			0,
			-0.525731112119E+00,
			0.850650808352E+00,
			0.850650808352E+00,
			0,
			0.525731112119E+00,
			-0.525731112119E+00,
			-0.850650808352E+00,
			0,
			0,
			0.525731112119E+00,
			-0.850650808352E+00,
			-0.850650808352E+00,
			0,
			-0.525731112119E+00,
			-0.525731112119E+00,
			0.850650808352E+00,
			0,
			0,
			0.525731112119E+00,
			0.850650808352E+00,
			-0.850650808352E+00,
			0,
			0.525731112119E+00,
			0.525731112119E+00,
			0.850650808352E+00,
			0,
			0,
			-0.525731112119E+00,
			-0.850650808352E+00
		];

		// convert to angles -- use these directions
		spher = cart.clump(3).collect({ | cart, i |
			cart.asCartesian.asSpherical.angles;
		});

		foa_a12_decoder_matrix =
		FoaEncoderMatrix.newDirections(spher).matrix.pseudoInverse;


		/////////// END code for 2nd order matrices /////////////////////

		prjDr = projDir;


		SynthDef(\blip, {
			var env = Env([0, 0.8, 1, 0], [0, 0.1, 0]);
			var blip = SinOsc.ar(1000) * EnvGen.kr(env, doneAction: 2);
			Out.ar(0, [blip, blip]);
		}).add;


		/// non ambisonc spatiaizers setup


		if (speaker_array.notNil) {

			var max_func, min_func, dimention, vbap_setup, adjust;

			nonambibus = outbus;

			numoutputs = speaker_array.size;

			max_func = { |x| // extract the highest value from an array
				var rep = 0;
				x.do{ |item|
					if(item > rep,
						{ rep = item };
					)
				};
				rep };

			case
			{ speaker_array[0].size < 2 || speaker_array[0].size > 3 }
			{ ^"bad speaker array".postln }
			{ speaker_array[0].size == 2 }
			{ dimention = 2;

				radiusses = Array.newFrom(speaker_array).collect({ |val| val[1] });
				longest_radius = max_func.value(radiusses);

				adjust = Array.fill(numoutputs, { |i|
					[(longest_radius - radiusses[i]) / 334, longest_radius/radiusses[i]];
				});

				lowest_elevation = 0;
				highest_elevation = 0;

				azimuths = speaker_array.collect({ |val| val.pop });

				vbap_setup = VBAPSpeakerArray(dimention, azimuths.flat);
			}
			{ speaker_array[0].size == 3 }
			{ dimention = 3;

				radiusses = Array.newFrom(speaker_array).collect({ |val| val[2] });
				longest_radius = max_func.value(radiusses);

				adjust = Array.fill(numoutputs, { |i|
					[(longest_radius - radiusses[i]) / 334, longest_radius/radiusses[i]];
				});

				min_func = { |x| // extract the lowest value from an array
					var rep = 0;
					x.do{ |item|
						if(item < rep,
							{ rep = item };
					) };
					rep };

				elevations = Array.newFrom(speaker_array).collect({ |val| val[1] });
				lowest_elevation = min_func.value(elevations);
				highest_elevation = max_func.value(elevations);

				speaker_array.collect({ |val| val.pop });

				vbap_setup = VBAPSpeakerArray(dimention, speaker_array);

				azimuths = speaker_array.collect({ |val| val.pop });
			};

			vbap_buffer = Buffer.loadCollection(server, vbap_setup.getSetsAndMatrices);

			perfectSphereFunc = { |sig|
				sig = Array.fill(numoutputs, { |i| DelayN.ar(sig[i],
					delaytime:adjust[i][0], mul:adjust[i][1]) });
			};
		} {

			var emulate_array, vbap_setup;

			numoutputs = 26;

			nonambibus = Bus.audio(server, numoutputs);

			emulate_array = [ [ 0, 90 ], [ 0, 45 ], [ 90, 45 ], [ 180, 45 ], [ -90, 45 ],
				[ 45, 35 ], [ 135, 35 ], [ -135, 35 ], [ -45, 35 ], [ 0, 0 ], [ 45, 0 ],
				[ 90, 0 ], [ 135, 0 ], [ 180, 0 ], [ -135, 0 ], [ -90, 0 ], [ -45, 0 ],
				[ 45, -35 ], [ 135, -35 ], [ -135, -35 ], [ -45, -35 ], [ 0, -45 ],
				[ 90, -45 ], [ 180, -45 ], [ -90, -45 ], [ 0, -90 ] ];

			vbap_setup = VBAPSpeakerArray(3, emulate_array);
			// emulate 26-point Lebedev grid

			vbap_buffer = Buffer.loadCollection(server, vbap_setup.getSetsAndMatrices);

			longest_radius = 18;
			lowest_elevation = -90;
			highest_elevation = 90;

			perfectSphereFunc = { |sig|
				sig;
			};

			SynthDef("nonAmbi2FuMa", {
				var sig = In.ar(nonambibus, numoutputs);
				sig = FoaEncode.ar(sig,
					FoaEncoderMatrix.newDirections(emulate_array.degrad));
				Out.ar(fumabus, sig);
			}).add;
		};


		// define ambisonic decoder


		if (suboutbus.notNil) {
			subOutFunc = { |signal, sublevel = 1|
				var subOut = Mix(signal) * sublevel * 0.5;
				Out.ar(suboutbus, subOut);
			};
		} {
			subOutFunc = { |signal, sublevel| };
		};


		if (decoder.isNil) {

			case
			{rawformat == \FuMa}
			{
				convert_fuma = false;
				convert_n3d = true;
				convert_direct = false;

				SynthDef("ambiConverter", { | gate = 1 |
					var n3dsig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					n3dsig = In.ar(n3dbus, bFormNumChan);
					n3dsig = HOAConvert.ar(maxorder, n3dsig, \ACN_N3D, \FuMa) * env;
					Out.ar(fumabus, n3dsig);
				}).add;

				SynthDef("globDecodeSynth",  { | sub = 1, level = 1 |
					var sig, nonambi;
					sig = In.ar(fumabus, bFormNumChan) * level;
					nonambi = In.ar(nonambibus, numoutputs) * level;
					perfectSphereFunc.value(nonambi);
					subOutFunc.value(sig + nonambi, sub);
					Out.ar(rawoutbus, sig);
					Out.ar(outbus, nonambi);
				}).add;

			}
			{rawformat == \N3D}
			{
				convert_fuma = true;
				convert_n3d = false;
				convert_direct = true;

				SynthDef("ambiConverter", { | gate = 1 |
					var sig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					sig = In.ar(fumabus, fourOrNine);
					sig = HOAConvert.ar(maxorder, sig, \FuMa, \ACN_N3D) * env;
					Out.ar(n3dbus, sig);
				}).add;

				SynthDef("globDecodeSynth", {
					| lf_hf=0, xover=400, sub = 1, level = 1 |
					var sig, nonambi;
					sig = In.ar(n3dbus, bFormNumChan) * level;
					nonambi = In.ar(nonambibus, numoutputs) * level;
					perfectSphereFunc.value(nonambi);
					subOutFunc.value(sig + nonambi, sub);
					Out.ar(rawoutbus, sig);
					Out.ar(outbus, nonambi);
				}).add;

			};

		} {

			case
			{ maxorder == 1 }
			{ convert_fuma = false;
				convert_n3d = true;
				convert_direct = false;

				SynthDef("ambiConverter", { | gate = 1 |
					var n3dsig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					n3dsig = In.ar(n3dbus, 4);
					n3dsig = FoaEncode.ar(n3dsig, n2m) * env;
					Out.ar(fumabus, n3dsig);
				}).add;

				if (decoder == "internal") {

					if (elevations.isNil) {
						elevations = Array.fill(numoutputs, { 0 });
					};

					SynthDef("globDecodeSynth",  { | sub = 1, level = 1 |
						var sig, nonambi;
						sig = In.ar(fumabus, 4);
						sig = BFDecode1.ar1(sig[0], sig[1], sig[2], sig[3],
							speaker_array.collect(_.degrad), elevations.collect(_.degrad),
							longest_radius, radiusses);
						nonambi = In.ar(nonambibus, numoutputs);
						perfectSphereFunc.value(nonambi);
						sig = (sig + nonambi) * level;
						subOutFunc.value(sig, sub);
						Out.ar(outbus, sig);
					}).add;

				} {

					if (speaker_array.notNil) {
						SynthDef("globDecodeSynth",  { | sub = 1, level = 1 |
							var sig, nonambi;
							sig = In.ar(fumabus, 4);
							sig = FoaDecode.ar(sig, decoder);
							nonambi = In.ar(nonambibus, numoutputs);
							perfectSphereFunc.value(nonambi);
							sig = (sig + nonambi) * level;
							subOutFunc.value(sig, sub);
							Out.ar(outbus, sig);
						}).add;

					} {
						SynthDef("globDecodeSynth",  { | sub = 1, level = 1 |
							var sig, nonambi;
							sig = In.ar(fumabus, 4);
							sig = FoaDecode.ar(sig, decoder);
							sig = sig * level;
							subOutFunc.value(sig, sub);
							Out.ar(outbus, sig);
						}).add;
					}
				}
			}

			{ maxorder == 2 }
			{
				if (decoder == "internal") {

					convert_fuma = false;
					convert_n3d = true;
					convert_direct = false;

					if (elevations.isNil) {
						elevations = Array.fill(numoutputs, { 0 });
					};

					SynthDef("ambiConverter", { | gate = 1 |
						var n3dsig, env;
						env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
						n3dsig = In.ar(n3dbus, 9);
						n3dsig = HOAConvert.ar(2, n3dsig, \ACN_N3D, \FuMa) * env;
						Out.ar(fumabus, n3dsig);
					}).add;

					SynthDef("globDecodeSynth",  { | sub = 1, level = 1 |
						var sig, nonambi;
						sig = In.ar(fumabus, 9);
						sig = FMHDecode1.ar1(sig[0], sig[1], sig[2], sig[3], sig[4],
							sig[5], sig[6], sig[7], sig[8],
							azimuths.collect(_.degrad), elevations.collect(_.degrad),
							longest_radius, radiusses);
						nonambi = In.ar(nonambibus, numoutputs);
						perfectSphereFunc.value(nonambi);
						sig = (sig + nonambi) * level;
						subOutFunc.value(sig, sub);
						Out.ar(outbus, sig);
					}).add;

				} { // assume ADT Decoder
					convert_fuma = true;
					convert_n3d = false;
					convert_direct = false;

					SynthDef("ambiConverter", { | gate = 1 |
						var sig, env;
						env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
						sig = In.ar(fumabus, fourOrNine);
						sig = HOAConvert.ar(maxorder, sig, \FuMa, \ACN_N3D) * env;
						Out.ar(n3dbus, sig);
					}).add;

					SynthDef("globDecodeSynth", {
						| lf_hf=0, xover=400, sub = 1, level = 1 |
						var sig, nonambi;
						sig = In.ar(n3dbus, bFormNumChan);
						sig = decoder.ar(sig[0], sig[1], sig[2], sig[3], sig[4],
							sig[5], sig[6], sig[7], sig[8], 0, lf_hf, xover:xover);
						nonambi = In.ar(nonambibus, numoutputs);
						perfectSphereFunc.value(nonambi);
						sig = (sig + nonambi) * level;
						subOutFunc.value(sig, sub);
						Out.ar(outbus, sig);
					}).add;
				};
			}

			{ maxorder == 3 } // assume ADT Decoder
			{ convert_fuma = true;
				convert_n3d = false;
				convert_direct = false;

				SynthDef("ambiConverter", { | gate = 1 |
					var sig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					sig = In.ar(fumabus, 9);
					sig = HOAConvert.ar(2, sig, \FuMa, \ACN_N3D) * env;
					Out.ar(n3dbus, sig);
				}).add;

				SynthDef("globDecodeSynth", {
					| lf_hf=0, xover=400, sub = 1, level = 1 |
					var sig, nonambi;
					sig = In.ar(n3dbus, bFormNumChan);
					sig = decoder.ar(sig[0], sig[1], sig[2], sig[3], sig[4],
						sig[5], sig[6], sig[7], sig[8], sig[9], sig[10], sig[11],
						sig[12], sig[13], sig[14], sig[15], 0, lf_hf, xover:xover);
					nonambi = In.ar(nonambibus, numoutputs);
					perfectSphereFunc.value(nonambi);
					sig = (sig + nonambi) * level;
					subOutFunc.value(sig, sub);
					Out.ar(outbus, sig);
				}).add;
			}

			{ maxorder == 4 } // assume ADT Decoder
			{ convert_fuma = true;
				convert_n3d = false;
				convert_direct = false;

				SynthDef("ambiConverter", { | gate = 1 |
					var sig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					sig = In.ar(fumabus, 9);
					sig = HOAConvert.ar(2, sig, \FuMa, \ACN_N3D) * env;
					Out.ar(n3dbus, sig);
				}).add;

				SynthDef("globDecodeSynth", {
					| lf_hf=0, xover=400, sub = 1, level = 1 |
					var sig, nonambi;
					sig = In.ar(n3dbus, bFormNumChan);
					sig = decoder.ar(sig[0], sig[1], sig[2], sig[3], sig[4],
						sig[5], sig[6], sig[7], sig[8], sig[9], sig[10], sig[11],
						sig[12], sig[13], sig[14],sig[15], sig[16], sig[17], sig[18],
						sig[19], sig[20], sig[21], sig[22], sig[23], sig[24],
						0, lf_hf, xover:xover);
					nonambi = In.ar(nonambibus, numoutputs);
					perfectSphereFunc.value(nonambi);
					sig = (sig + nonambi) * level;
					subOutFunc.value(sig, sub);
					Out.ar(outbus, sig);
				}).add;
			}

			{ maxorder == 5 } // assume ADT Decoder
			{ convert_fuma = true;
				convert_n3d = false;
				convert_direct = false;

				SynthDef("ambiConverter", { | gate = 1 |
					var sig, env;
					env = EnvGen.kr(Env.asr(curve:\hold), gate, doneAction:2);
					sig = In.ar(fumabus, 9);
					sig = HOAConvert.ar(2, sig, \FuMa, \ACN_N3D) * env;
					Out.ar(n3dbus, sig);
				}).add;

				SynthDef("globDecodeSynth", {
					| lf_hf=0, xover=400, sub = 1, level = 1 |
					var sig, nonambi;
					sig = In.ar(n3dbus, bFormNumChan);
					sig = decoder.ar(sig[0], sig[1], sig[2], sig[3], sig[4],
						sig[5], sig[6], sig[7], sig[8], sig[9], sig[10], sig[11],
						sig[12], sig[13], sig[14], sig[15], sig[16], sig[17],
						sig[18], sig[19], sig[20], sig[21], sig[22], sig[23],
						sig[24], sig[15], sig[16], sig[17], sig[18], sig[19],
						sig[20], sig[21], sig[22], sig[23], sig[24], sig[25],
						sig[26], sig[27], sig[28], sig[29], sig[30], sig[31],
						sig[32], sig[33], sig[34], sig[35],
						0, lf_hf, xover:xover);
					nonambi = In.ar(nonambibus, numoutputs);
					perfectSphereFunc.value(nonambi);
					sig = (sig + nonambi) * level;
					subOutFunc.value(sig, sub);
					Out.ar(outbus, sig);
				}).add;
			};
		};

		// Make File-in SynthDefs

		playInFunc = Array.newClear(4);
		// one for File, Stereo, BFormat, Stream - streamed file;

		playInFunc[0] = { | playerRef, busini, bufnum, tpos, lp = 0, rate, channum |
			// Note it needs all the variables
			var spos = tpos * BufSampleRate.kr(bufnum),
			scaledRate = rate * BufRateScale.kr(bufnum);
			playerRef.value = PlayBuf.ar(channum, bufnum, scaledRate, startPos: spos,
				loop: lp, doneAction:2);
		};

		// Make HWBus-in SynthDefs

		playInFunc[1] = { | playerRef, busini, bufnum, tpos, lp = 0, rate, channum |
			playerRef.value = In.ar(busini + server.inputBus.index, channum);
		};

		// Make SCBus-in SynthDefs

		playInFunc[2] = { | playerRef, busini, bufnum, tpos, lp = 0, rate, channum |
			playerRef.value = In.ar(busini, channum);
		};

		playInFunc[3] = { | playerRef, busini, bufnum, tpos, lp = 0, rate, channum |
			// Note it needs all the variables
			var trig;
			playerRef.value = DiskIn.ar(channum, bufnum, lp);
			trig = Done.kr(playerRef.value);
			FreeSelf.kr(trig);
		};

		spatFuncs = Array.newClear(spatList.size);
		// contains the synthDef blocks for each spatialyers lib

		// Ambitools
		spatFuncs[0] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			ref.value = HOAEncoder.ar(maxorder,
				(ref.value + input), CircleRamp.kr(azimuth, 0.1, -pi, pi),
				Lag.kr(elevation), 0, 1, Lag.kr(radius), longest_radius);
		};

		// HoaLib
		spatFuncs[1] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var sig = LPF.ar(input, (1 - distance) * 18000 + 2000);
			// attenuate high freq with distance
			ref.value = HOALibEnc3D.ar(maxorder,
				(ref.value + sig) * Lag.kr(longest_radius / radius),
				CircleRamp.kr(azimuth, 0.1, -pi, pi), Lag.kr(elevation), 0);
		};

		// ADTB
		spatFuncs[2] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var sig = LPF.ar(input, (1 - distance) * 18000 + 2000);
			// attenuate high freq with distance
			ref.value = HOAmbiPanner.ar(maxorder,
				(ref.value + sig) * Lag.kr(longest_radius / radius),
				CircleRamp.kr(azimuth, 0.1, -pi, pi), Lag.kr(elevation), 0);
		};

		// ATK
		spatFuncs[3] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var diffuse, spread, omni,
			sig = LPF.ar(input, (1 - distance) * 18000 + 2000),
			// attenuate high freq with distance
			rad = Lag.kr(longest_radius / radius);
			sig = (sig + ref.value) * rad;
			omni = FoaEncode.ar(sig, foaEncoderOmni);
			spread = FoaEncode.ar(sig, foaEncoderSpread);
			diffuse = FoaEncode.ar(sig, foaEncoderDiffuse);
			sig = Select.ar(difu, [omni, diffuse]);
			sig = Select.ar(spre, [sig, spread]);
			sig = FoaTransform.ar(sig, 'push', halfPi * contract, azimuth, elevation);
			sig = HPF.ar(sig, 20); // stops bass frequency blow outs by proximity
			ref.value = FoaTransform.ar(sig, 'proximity', radius);
		};

		// BF-FMH
		spatFuncs[4] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var sig = LPF.ar(input, (1 - distance) * 18000 + 2000);
			// attenuate high freq with distance
			ref.value = bfOrFmh.ar(ref.value + sig, azimuth, elevation,
				Lag.kr(longest_radius / radius), 0.5);
		};

		// joshGrain
		spatFuncs[5] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var sig = LPF.ar(input, (1 - distance) * 18000 + 2000);
			// attenuate high freq with distance
			ref.value = MonoGrainBF.ar(ref.value + sig, win, rate, rand,
				azimuth, 1 - contract, elevation, 1 - contract,
				rho: Lag.kr(longest_radius / radius),
				mul: ((0.5 - win) + (1 - (rate / 40))).clip(0, 1) * 0.5 );
		};

		// VBAP
		spatFuncs[6] = { |ref, input, radius, distance, azimuth, elevation, difu, spre,
			contract, win, rate, rand|
			var sig = LPF.ar(input, (1 - distance) * 18000 + 2000),
			// attenuate high freq with distance
			azi = azimuth * rad2deg, // convert to degrees
			elev = elevation * rad2deg, // convert to degrees
			elevexcess = Select.kr(elev < lowest_elevation, [0, elev.abs]);
			elevexcess = Select.kr(elev > highest_elevation, [0, elev]);
			// get elevation overshoot
			elev = elev.clip(lowest_elevation, highest_elevation);
			// restrict between min & max

			ref.value = VBAP.ar(numoutputs,
				(ref.value + sig) * (longest_radius / radius),
				vbap_buffer.bufnum, CircleRamp.kr(azi, 0.1, -180, 180), Lag.kr(elevation),
				((1 - contract) + (elevexcess / 90)) * 100) * 0.5;
		};

		outPutFuncs = Array.newClear(3);
		// contains the synthDef blocks for each spatialyers

		outPutFuncs[0] = { |dry, wet, globrev|
			Out.ar(gbixfbus, wet * globrev);
			Out.ar(n3dbus, wet);
		};

		outPutFuncs[1] = { |dry, wet, globrev|
			Out.ar(gbfbus, wet * globrev);
			Out.ar(fumabus, wet);
		};

		outPutFuncs[2] = { |dry, wet, globrev|
			Out.ar(gbus, dry * globrev);
			Out.ar(nonambibus, wet);
		};


		makeSpatialisers = { | rev_type |
			var out_type = 0;

			spatList.do { |item, i|

				case
				{ i <= lastN3D } { out_type = 0 }
				{ (i > lastN3D) && (i <= lastFUMA) } { out_type = 1 }
				{ i > lastFUMA } { out_type = 2 };

				playList.do { |play_type, j|

					SynthDef(item++play_type++rev_type, {
						| bufnum = 0, rate = 1, tpos = 0, lp = 0, busini,
						azim = 0, elev = 0, radius = 200, level = 1,
						dopamnt = 0, glev = 0, llev = 0,
						insertFlag = 0, insertOut, insertBack,
						room = 0.5, damp = 05, wir, df, sp,
						contr = 1, grainrate = 10, winsize = 0.1, winrand = 0 |

						var rad = Lag.kr(radius),
						dis = rad * 0.01,
						globallev = (1 / dis.sqrt) - 1, //global reverberation
						locallev, lrevRef = Ref(0),
						az = azim - halfPi,
						p = Ref(0),
						rd = dis * 340, // Doppler
						cut = ((1 - dis) * 2).clip(0, 1);
						//make shure level is 0 when radius reaches 100
						rad = rad.clip(1, 50);

						playInFunc[j].value(p, busini, bufnum, tpos, lp, rate, 1);
						p = p * level;
						p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

						localReverbFunc.value(lrevRef, p, wir, dis * llev,
							// local reverberation
							room, damp);

						spatFuncs[i].value(lrevRef, p, rad, dis, az, elev, df, sp, contr,
							winsize, grainrate, winrand);

						outPutFuncs[out_type].value(p * cut, lrevRef.value * cut,
							globallev.clip(0, 1) * glev);
					}).add;


					SynthDef(item++"Stereo"++play_type++rev_type, {
						| bufnum = 0, rate = 1, tpos = 0, lp = 0, busini,
						azim = 0, elev = 0, radius = 0, level = 1,
						dopamnt = 0, glev = 0, llev = 0, angle = 1.05,
						insertFlag = 0, insertOut, insertBack,
						room = 0.5, damp = 05, wir, df, sp,
						contr = 1, grainrate = 10, winsize = 0.1, winrand = 0 |

						var rad = Lag.kr(radius),
						dis = rad * 0.01,
						globallev = (1 / dis.sqrt) - 1, //global reverberation
						lrev1Ref = Ref(0), lrev2Ref = Ref(0),
						az = Lag.kr(azim - halfPi),
						p = Ref(0),
						rd = dis * 340, // Doppler
						cut = ((1 - dis) * 2).clip(0, 1);
						//make shure level is 0 when radius reaches 100
						rad = rad.clip(1, 50);

						playInFunc[j].value(p, busini, bufnum, tpos, lp, rate, 2);
						p = p * level;
						p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

						localReverbStereoFunc.value(lrev1Ref, lrev2Ref, p[0], p[1],
							wir, dis * llev, room, damp);

						spatFuncs[i].value(lrev1Ref, p[0], rad, dis, az - (angle * (1 - dis)),
							elev, df, sp, contr, winsize, grainrate, winrand);
						spatFuncs[i].value(lrev2Ref, p[1], rad, dis, az + (angle * (1 - dis)),
							elev, df, sp, contr, winsize, grainrate, winrand);

						outPutFuncs[out_type].value(Mix.ar(p) * 0.5 * cut,
							(lrev1Ref.value + lrev2Ref.value) * 0.5 * cut,
							globallev.clip(0, 1) * glev);
					}).add;

					if (item == "ATK") {

						SynthDef(\ATKBFormat++play_type++4, {
							| bufnum = 0, rate = 1, tpos = 0, lp = 0, busini,
							azim = 0, elev = 0, radius = 200, level = 1,
							dopamnt = 0, glev = 0, llev = 0,
							insertFlag = 0, insertOut, insertBack,
							room = 0.5, damp = 05, wir, df, sp,
							contr = 0, directang = 1, rotAngle = 0 |

							var rad = Lag.kr(radius),
							dis = rad * 0.01,
							pushang = dis * halfPi, // degree of sound field displacement
							globallev = (1 / dis.sqrt) - 1, //global reverberation
							locallev, lrevRef = Ref(0),
							az = azim - halfPi,
							p = Ref(0),
							rd = dis * 340, // Doppler
							cut = ((1 - dis) * 2).clip(0, 1);
							//make shure level is 0 when radius reaches 100
							rad = rad.clip(1, 50);

							playInFunc[j].value(p, busini, bufnum, tpos, lp, rate, 4);
							p = p * level;
							p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

							localReverbFunc.value(lrevRef, p[0], wir, dis * llev, room, damp);
							// local reverberation

							p = FoaDirectO.ar(lrevRef.value + p, directang);
							// directivity
							p = FoaTransform.ar(p, 'rotate', rotAngle);
							p = FoaTransform.ar(p, 'push', pushang, az, elev);

							p = p * cut;

							outPutFuncs[1].value(p, p,
								globallev.clip(0, 1) * glev);
						}).add;
					};

					if (item == "Ambitools") {

						SynthDef(\AmbitoolsBFormat++play_type++4, {
							| bufnum = 0, rate = 1, tpos = 0, lp = 0, busini,
							azim = 0, elev = 0, radius = 200, level = 1,
							dopamnt = 0, glev = 0, llev = 0,
							insertFlag = 0, insertOut, insertBack,
							room = 0.5, damp = 05, wir, df, sp,
							contr = 0, rotAngle = 0|

							var rad = Lag.kr(radius),
							dis = rad * 0.01,
							pushang = dis * halfPi, // degree of sound field displacement
							globallev = (1 / dis.sqrt) - 1, //global reverberation
							locallev, lrevRef = Ref(0),
							az = azim - halfPi,
							p = Ref(0),
							rd = dis * 340, // Doppler
							cut = ((1 - dis) * 2).clip(0, 1);
							//make shure level is 0 when radius reaches 100
							rad = rad.clip(1, 50);

							playInFunc[j].value(p, busini, bufnum, tpos, lp, rate, 4);
							p = p * level;
							p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

							localReverbFunc.value(lrevRef, p[0], wir, dis * llev, room, damp);
							// local reverberation

							p = FoaEncode.ar(lrevRef.value + p, n2m);
							p = HOATransRotateAz.ar(1, p, rotAngle);
							p = HOABeamDirac2Hoa.ar(1, p, az, elev, focus:pushang);

							p = p * cut;

							outPutFuncs[0].value(p, p,
								globallev.clip(0, 1) * glev);
						}).add;

						[9, 16, 25, 36].do { |item, count|
							var ord = (item.sqrt) - 1;

							SynthDef(\AmbitoolsBFormat++play_type++item, {
								| bufnum = 0, rate = 1, tpos = 0, lp = 0, busini,
								azim = 0, elev = 0, radius = 200, level = 1,
								dopamnt = 0, glev = 0, llev = 0,
								insertFlag = 0, insertOut, insertBack,
								room = 0.5, damp = 05, wir, df, sp,
								contr = 0, rotAngle = 0|

								var rad = Lag.kr(radius),
								dis = rad * 0.01,
								pushang = dis * halfPi, // degree of sound field displacement
								globallev = (1 / dis.sqrt) - 1, //global reverberation
								locallev, lrevRef = Ref(0),
								az = azim - halfPi,
								p = Ref(0),
								rd = dis * 340, // Doppler
								cut = ((1 - dis) * 2).clip(0, 1);
								//make shure level is 0 when radius reaches 100
								rad = rad.clip(1, 50);

								playInFunc[j].value(p, busini, bufnum, tpos, lp, rate, item);
								p = p * level;
								p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

								localReverbFunc.value(lrevRef, p[0], wir, dis * llev, room, damp);
								// local reverberation

								p = HOATransRotateAz.ar(ord, lrevRef.value + p, rotAngle);
								p = HOABeamDirac2Hoa.ar(ord, p, az, elev, focus:pushang);

								p = p * cut;

								outPutFuncs[0].value(p, p,
									globallev.clip(0, 1) * glev);
							}).add;
						};
					};

				};

				//	SynthDef("playBFormatATK"++type++"_4", {
				// 		| bufnum = 0, rate = 1, level = 1, tpos = 0, lp = 0,
				// 		rotAngle = 0, azim = 0, elev = 0, radius = 200,
				// 		glev, llev, directang = 0, contr, dopamnt, busini,
				// 		insertFlag = 0, insertOut, insertBack |
				//
				// 		var playerRef = Ref(0),
				// 		pushang, az, ele, globallev,
				// 		rd, dis = radius.clip(0.01, 1);
				//
				// 		az = azim - halfPi;
				// 		pushang = dis * halfPi; // degree of sound field displacement
				//
				// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 4);
				// 		playerRef.value = LPF.ar(playerRef.value, (1 - dis) * 18000 + 2000);
				// 		// attenuate high freq with distance
				// 		rd = Lag.kr(dis * 340); 				 // Doppler
				// 		playerRef.value = DelayC.ar(playerRef.value, 0.2, rd/1640.0 * dopamnt);
				//
				// 		playerRef.value = FoaDirectO.ar(playerRef.value, directang);
				// 		// directivity
				// 		playerRef.value = FoaTransform.ar(playerRef.value, 'rotate', rotAngle);
				// 		playerRef.value = FoaTransform.ar(playerRef.value, 'push',
				// 		pushang, az, ele);
				//
				// 		globallev = (1 / radius.sqrt) - 1; // lower tail of curve to zero
				// 		outPutFuncs[1].value(nil, playerRef.value, globallev);
				// 	}).add;
				//
				//
				// 	SynthDef("playBFormatAmbitools"++type++"_4", {
				// 		| outbus, bufnum = 0, rate = 1,
				// 		level = 1, tpos = 0, lp = 0, rotAngle = 0,
				// 		azim = 0, elev = 0, radius = 0,
				// 		glev, llev, directang = 0, contr, dopamnt,
				// 		busini, insertFlag = 0 |
				//
				// 		var playerRef, wsinal, pushang = 0,
				// 		aFormatFoa, aFormatSoa, ambSigFoaProcessed, ambSigSoaProcessed,
				//
				// 		az, ele, dis, globallev, locallev,
				// 		gsig, //lsig, intens,
				// 		rd;
				//
				// 		dis = radius;
				//
				// 		az = azim - halfPi;
				// 		az = CircleRamp.kr(az, 0.1, -pi, pi);
				// 		ele = Lag.kr(elev);
				// 		// ele = elev;
				// 		dis = Select.kr(dis < 0, [dis, 0]);
				// 		dis = Select.kr(dis > 1, [dis, 1]);
				// 		playerRef = Ref(0);
				// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 4);
				//
				// 		rd = Lag.kr(dis * 340);
				// 		playerRef.value = DelayC.ar(playerRef.value, 0.2, rd/1640.0 * dopamnt);
				//
				// 		wsinal = playerRef.value[0] * contr * level * dis * 2.0;
				//
				// 		//Out.ar(outbus, wsinal);
				//
				// 		// global reverb
				// 		globallev = 1 / dis.sqrt;
				// 		/*intens = globallev - 1;
				// 		intens = intens.clip(0, 4);
				// 		intens = intens * 0.25;*/
				//
				// 		playerRef.value = FoaDecode.ar(playerRef.value,
				// 		FoaDecoderMatrix.newAmbix1);
				// 		playerRef.value = HOATransRotateAz.ar(1, playerRef.value, rotAngle);
				// 		playerRef.value = HOABeamDirac2Hoa.ar(1, playerRef.value, 1, az, ele,
				// 		focus:contr * dis.sqrt) * (1 - dis.squared) * level;
				//
				// 		Out.ar(n3dbus, playerRef.value);
				//
				// 		globallev = globallev - 1.0; // lower tail of curve to zero
				// 		globallev = globallev.clip(0, 1);
				// 		globallev = globallev * glev * 6;
				//
				// 		gsig = playerRef.value[0] * globallev;
				//
				// 		//locallev = dis  * llev * 5;
				// 		//lsig = playerRef.value[0] * locallev;
				//
				// 		//gsig = (playerRef.value * globallev) + (playerRef.value * locallev);
				// 		// b-format
				// 		Out.ar(gbixfbus, gsig);
				// 	}).add;

				/*				SynthDef("ATK2Chowning"++rev_type, {
				| inbus, radius = 200,
				dopamnt = 0, glev = 0, llev = 0,
				insertFlag = 0, insertOut, insertBack,
				room = 0.5, damp = 05, wir|

				var rad = Lag.kr(radius),
				dis = rad * 0.01,
				globallev = (1 / dis.sqrt) - 1, //global reverberation
				lrevRef = Ref(0),
				p = In.ar(inbus, 1),
				rd = radius * 340, // Doppler
				cut = ((1 - dis) * 2).clip(0, 1);
				//make shure level is 0 when radius reaches plim
				rad = rad.clip(1, 50);

				p = DelayC.ar(p, 0.2, rd/1640.0 * dopamnt);

				localReverbFunc.value(lrevRef, p, wir, dis * llev, room, damp);
				p = HPF.ar(p, 20); // stops bass frequency blow outs by proximity
				p = FoaTransform.ar(p + lrevRef.value, 'proximity',
				rad * 50);

				outPutFuncs[out_type].value(p * cut, lrevRef.value * cut,
				globallev.clip(0, 1) * glev);
				}).add;*/

			}
		}; //end makeSpatialisers

		// makeSynthDefPlayers = { | type, i = 0 |
		// 	// 3 types : File, HWBus and SWBus - i duplicates with 0, 1 & 2
		//
		// 	SynthDef("playMono"++type, { | outbus, bufnum = 0, rate = 1,
		// 		level = 1, tpos = 0, lp = 0, busini |
		// 		var playerRef = Ref(0);
		// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 1);
		// 		Out.ar(outbus, playerRef.value * level);
		// 	}).add;
		//
		// 	SynthDef("playStereo"++type, { | outbus, bufnum = 0, rate = 1,
		// 		level = 1, tpos = 0, lp = 0, busini |
		// 		var playerRef = Ref(0);
		// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 2);
		// 		Out.ar(outbus, playerRef.value * level);
		// 	}).add;
		//
		//
		// 	SynthDef("playBFormatATK"++type++"_4", {
		// 		| bufnum = 0, rate = 1, level = 1, tpos = 0, lp = 0,
		// 		rotAngle = 0, azim = 0, elev = 0, radius = 200,
		// 		glev, llev, directang = 0, contr, dopamnt, busini,
		// 		insertFlag = 0, insertOut, insertBack |
		//
		// 		var playerRef = Ref(0),
		// 		pushang, az, ele, globallev,
		// 		rd, dis = radius.clip(0.01, 1);
		//
		// 		az = azim - halfPi;
		// 		pushang = dis * halfPi; // degree of sound field displacement
		//
		// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 4);
		// 		playerRef.value = LPF.ar(playerRef.value, (1 - dis) * 18000 + 2000);
		// 		// attenuate high freq with distance
		// 		rd = Lag.kr(dis * 340); 				 // Doppler
		// 		playerRef.value = DelayC.ar(playerRef.value, 0.2, rd/1640.0 * dopamnt);
		//
		// 		playerRef.value = FoaDirectO.ar(playerRef.value, directang);
		// 		// directivity
		// 		playerRef.value = FoaTransform.ar(playerRef.value, 'rotate', rotAngle);
		// 		playerRef.value = FoaTransform.ar(playerRef.value, 'push',
		// 		pushang, az, ele);
		//
		// 		globallev = (1 / radius.sqrt) - 1; // lower tail of curve to zero
		// 		outPutFuncs[1].value(nil, playerRef.value, globallev);
		// 	}).add;
		//
		//
		// 	SynthDef("playBFormatAmbitools"++type++"_4", {
		// 		| outbus, bufnum = 0, rate = 1,
		// 		level = 1, tpos = 0, lp = 0, rotAngle = 0,
		// 		azim = 0, elev = 0, radius = 0,
		// 		glev, llev, directang = 0, contr, dopamnt,
		// 		busini, insertFlag = 0 |
		//
		// 		var playerRef, wsinal, pushang = 0,
		// 		aFormatFoa, aFormatSoa, ambSigFoaProcessed, ambSigSoaProcessed,
		//
		// 		az, ele, dis, globallev, locallev,
		// 		gsig, //lsig, intens,
		// 		rd;
		//
		// 		dis = radius;
		//
		// 		az = azim - halfPi;
		// 		az = CircleRamp.kr(az, 0.1, -pi, pi);
		// 		ele = Lag.kr(elev);
		// 		// ele = elev;
		// 		dis = Select.kr(dis < 0, [dis, 0]);
		// 		dis = Select.kr(dis > 1, [dis, 1]);
		// 		playerRef = Ref(0);
		// 		playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, 4);
		//
		// 		rd = Lag.kr(dis * 340);
		// 		playerRef.value = DelayC.ar(playerRef.value, 0.2, rd/1640.0 * dopamnt);
		//
		// 		wsinal = playerRef.value[0] * contr * level * dis * 2.0;
		//
		// 		//Out.ar(outbus, wsinal);
		//
		// 		// global reverb
		// 		globallev = 1 / dis.sqrt;
		// 		/*intens = globallev - 1;
		// 		intens = intens.clip(0, 4);
		// 		intens = intens * 0.25;*/
		//
		// 		playerRef.value = FoaDecode.ar(playerRef.value,
		// 		FoaDecoderMatrix.newAmbix1);
		// 		playerRef.value = HOATransRotateAz.ar(1, playerRef.value, rotAngle);
		// 		playerRef.value = HOABeamDirac2Hoa.ar(1, playerRef.value, 1, az, ele,
		// 		focus:contr * dis.sqrt) * (1 - dis.squared) * level;
		//
		// 		Out.ar(n3dbus, playerRef.value);
		//
		// 		globallev = globallev - 1.0; // lower tail of curve to zero
		// 		globallev = globallev.clip(0, 1);
		// 		globallev = globallev * glev * 6;
		//
		// 		gsig = playerRef.value[0] * globallev;
		//
		// 		//locallev = dis  * llev * 5;
		// 		//lsig = playerRef.value[0] * locallev;
		//
		// 		//gsig = (playerRef.value * globallev) + (playerRef.value * locallev);
		// 		// b-format
		// 		Out.ar(gbixfbus, gsig);
		// 	}).add;
		//
		//
		// 	[9, 16, 25, 36].do { |item, count|
		//
		// 		SynthDef("playBFormatATK"++type++"_"++item, {
		// 			| outbus, bufnum = 0, rate = 1,
		// 			level = 1, tpos = 0, lp = 0, rotAngle = 0,
		// 			azim = 0, elev = 0, radius = 0,
		// 			glev, llev, directang = 0, contr, dopamnt,
		// 			busini, insertFlag = 0, aFormatBusOutFoa, aFormatBusInFoa,
		// 			aFormatBusOutSoa, aFormatBusInSoa |
		//
		// 			var playerRef, wsinal, pushang = 0,
		// 			aFormatFoa, aFormatSoa, ambSigFoaProcessed, ambSigSoaProcessed,
		//
		// 			az, ele, dis, globallev, locallev,
		// 			gsig, lsig, rd,
		// 			intens;
		// 			dis = radius;
		//
		// 			az = azim - halfPi;
		// 			// az = CircleRamp.kr(az, 0.1, -pi, pi);
		// 			// ele = Lag.kr(elev);
		// 			ele = elev;
		// 			pushang = dis * halfPi; // degree of sound field displacement
		// 			dis = Select.kr(dis < 0, [dis, 0]);
		// 			dis = Select.kr(dis > 1, [dis, 1]);
		// 			playerRef = Ref(0);
		// 			playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, item);
		//
		// 			rd = Lag.kr(dis * 340);
		// 			playerRef.value = DelayC.ar(playerRef.value, 0.2,
		// 			rd/1640.0 * dopamnt);
		//
		// 			wsinal = playerRef.value[0] * contr * Lag.kr(level) * dis * 2.0;
		//
		// 			Out.ar(outbus, wsinal);
		//
		// 			// global reverb
		// 			globallev = 1 / dis.sqrt;
		// 			intens = globallev - 1;
		// 			intens = intens.clip(0, 4);
		// 			intens = intens * 0.25;
		//
		// 			playerRef.value = FoaEncode.ar(playerRef.value,
		// 			n2m);
		// 			playerRef.value = FoaDirectO.ar(playerRef.value, directang);
		// 			// directivity
		//
		// 			playerRef.value = FoaTransform.ar(playerRef.value, 'rotate', rotAngle,
		// 			Lag.kr(level) * intens * (1 - contr));
		//
		// 			playerRef.value = FoaTransform.ar(playerRef.value, 'push', pushang,
		// 			az, ele);
		//
		// 			// convert to A-format and send to a-format out busses
		// 			aFormatFoa = FoaDecode.ar(playerRef.value, b2a);
		// 			Out.ar(aFormatBusOutFoa, aFormatFoa);
		// 			// aFormatSoa = AtkMatrixMix.ar(ambSigSoa, soa_a12_decoder_matrix);
		// 			// Out.ar(aFormatBusOutSoa, aFormatSoa);
		//
		// 			// flag switchable selector of a-format signal (from insert or not)
		// 			aFormatFoa = Select.ar(insertFlag, [aFormatFoa,
		// 			InFeedback.ar(aFormatBusInFoa, 4)]);
		// 			//aFormatSoa = Select.ar(insertFlag, [aFormatSoa,
		// 			//InFeedback.ar(aFormatBusInSoa, 12)]);
		//
		// 			// convert back to b-format
		// 			ambSigFoaProcessed = FoaEncode.ar(aFormatFoa, a2b);
		// 			//ambSigSoaProcessed = AtkMatrixMix.ar(aFormatSoa,
		// 			//soa_a12_encoder_matrix);
		//
		// 			// not sure if the b2a/a2b process degrades signal.
		// 			// Just in case it does:
		// 			playerRef.value = Select.ar(insertFlag, [playerRef.value,
		// 			ambSigFoaProcessed]);
		// 			//ambSigSoa = Select.ar(insertFlag, [ambSigSoa, ambSigSoaProcessed]);
		//
		// 			Out.ar(fumabus, playerRef.value);
		//
		// 			globallev = globallev - 1.0; // lower tail of curve to zero
		// 			globallev = globallev.clip(0, 1);
		// 			globallev = globallev * glev * 6;
		// 			gsig = playerRef.value[0] * globallev;
		//
		// 			locallev = dis * llev * 5;
		// 			lsig = playerRef.value[0] * locallev;
		//
		// 			gsig = (playerRef.value * globallev) + (playerRef.value * locallev);
		// 			// b-format
		// 			Out.ar(gbfbus, gsig);
		// 		}).add;
		//
		//
		// 		SynthDef("playBFormatAmbitools"++type++"_"++item, {
		// 			| outbus, bufnum = 0, rate = 1,
		// 			level = 1, tpos = 0, lp = 0, rotAngle = 0,
		// 			azim = 0, elev = 0, radius = 0,
		// 			glev, llev, directang = 0, contr, dopamnt,
		// 			busini, insertFlag = 0 |
		//
		// 			var playerRef, wsinal, pushang = 0,
		// 			aFormatFoa, aFormatSoa, ambSigFoaProcessed, ambSigSoaProcessed,
		//
		// 			az, ele, dis, globallev, locallev, gsig, //lsig, intens,
		// 			rd;
		// 			dis = radius;
		//
		// 			az = azim - halfPi;
		// 			az = CircleRamp.kr(az, 0.1, -pi, pi);
		// 			ele = Lag.kr(elev);
		// 			// ele = elev;
		// 			dis = Select.kr(dis < 0, [dis, 0]);
		// 			dis = Select.kr(dis > 1, [dis, 1]);
		// 			playerRef = Ref(0);
		// 			playInFunc[i].value(playerRef, busini, bufnum, tpos, lp, rate, item);
		//
		// 			rd = Lag.kr(dis * 340);
		// 			playerRef.value = DelayC.ar(playerRef.value, 0.2,
		// 			rd/1640.0 * dopamnt);
		//
		// 			wsinal = playerRef.value[0] * contr * level * dis * 2.0;
		//
		// 			Out.ar(outbus, wsinal);
		//
		// 			// global reverb
		// 			globallev = 1 / dis.sqrt;
		// 			/*intens = globallev - 1;
		// 			intens = intens.clip(0, 4);
		// 			intens = intens * 0.25;*/
		//
		// 			playerRef.value = HOATransRotateAz.ar(count + 2, playerRef.value,
		// 			rotAngle);
		// 			playerRef.value = HOABeamDirac2Hoa.ar(count + 2, playerRef.value, 1,
		// 				az, ele,
		// 			focus:contr * dis.sqrt) * (1 - dis.squared) * level;
		//
		// 			Out.ar(n3dbus, playerRef.value);
		//
		// 			globallev = globallev - 1.0; // lower tail of curve to zero
		// 			globallev = globallev.clip(0, 1);
		// 			globallev = globallev * glev * 6;
		// 			gsig = playerRef.value[0] * globallev;
		//
		// 			Out.ar(gbixfbus, gsig);
		// 		}).add;
		// 	};
		//
		// }; //end makeSynthDefPlayers

		// allpass reverbs
		if (maxorder == 1) {

			SynthDef("revGlobalAmb_pass", { | gate = 1, room = 0.5, damp = 0.5 |
				var env, temp, convsig, sig, sigx, sigf = In.ar(gbfbus, 4);
				sigx = In.ar(gbixfbus, 4);
				sig = In.ar(gbus, 1);
				sigx = FoaEncode.ar(sigx, n2m);
				env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
				sig = sig + sigf + sigx;
				sig = FoaDecode.ar(sig, b2a);
				16.do({ sig = AllpassC.ar(sig, 0.08, room * { Rand(0, 0.08) }.dup(4) +
					{ Rand(0, 0.001) },
					damp * 2)});
				sig = FoaEncode.ar(sig, a2b);
				sig = sig * env;
				Out.ar(fumabus, sig);
			}).add;

		} {

			SynthDef("revGlobalAmb_pass", { | gate = 1, room = 0.5, damp = 0.5 |
				var env, w, x, y, z, r, s, t, u, v,
				soaSig, tmpsig, sig, sigx, sigf = In.ar(gbfbus, 9);
				sigx = In.ar(gbixfbus, 9);
				sigx = HOAConvert.ar(2, sigx, \FuMa, \ACN_N3D);
				sig = In.ar(gbus, 1);
				env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
				sig = sig + sigf + sigx;
				sig = AtkMatrixMix.ar(sig, soa_a12_decoder_matrix);
				16.do({ sig = AllpassC.ar(sig, 0.08, room * { Rand(0, 0.08) }.dup(12) +
					{ Rand(0, 0.001) },
					damp * 2)});
				#w, x, y, z, r, s, t, u, v = AtkMatrixMix.ar(sig, soa_a12_encoder_matrix)
				* env;
				soaSig = [w, x, y, z, r, s, t, u, v];
				Out.ar(fumabus, soaSig);
			}).load(server);
		};

		//run the makeSpatialisers function for each types of local reverbs

		localReverbFunc = { | lrevRef, p, rirWspectrum, locallev, room, damp |
			var temp = p;
			16.do({ temp = AllpassC.ar(temp, 0.08, room * { Rand(0, 0.08) } +
				{ Rand(0, 0.001) },
				damp * 2)});
			lrevRef.value = temp * locallev;
		};

		localReverbStereoFunc = { | lrev1Ref, lrev2Ref, p1, p2, rirZspectrum,
			locallev, room, damp |
			var temp1 = p1, temp2 = p2;
			8.do({ temp1 = AllpassC.ar(temp1, 0.08, room * { Rand(0, 0.08) } +
				{ Rand(0, 0.001) },
				damp * 2)});
			8.do({ temp2 = AllpassC.ar(temp2, 0.08, room * { Rand(0, 0.08) } +
				{ Rand(0, 0.001) },
				damp * 2)});
			lrev1Ref.value = temp1 * locallev;
			lrev2Ref.value = temp2 * locallev;
		};
		//
		// localReverbBFormatFunc = { | lrev1Ref, lrev2Ref, p1, p2, rirZspectrum,
		// 	locallev, room, damp |
		// 	var temp1 = p1, temp2 = p2;
		// 	8.do({ temp1 = AllpassC.ar(temp1, 0.08, room * { Rand(0, 0.08) } +
		// 		{ Rand(0, 0.001) },
		// 	damp * 2)});
		// 	8.do({ temp2 = AllpassC.ar(temp2, 0.08, room * { Rand(0, 0.08) } +
		// 		{ Rand(0, 0.001) },
		// 	damp * 2)});
		// 	lrev1Ref.value = temp1 * locallev;
		// 	lrev2Ref.value = temp2 * locallev;
		// };

		makeSpatialisers.value(rev_type:"_pass");

		// freeverb defs

		if (maxorder == 1) {

			SynthDef("revGlobalAmb_free",  { | gate = 1, room = 0.5, damp = 0.5 |
				var env, temp, convsig, sig, sigx, sigf = In.ar(gbfbus, 4);
				sigx = In.ar(gbixfbus, 4);
				sig = In.ar(gbus, 1);
				sigx = FoaEncode.ar(sigx, n2m);
				env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
				sig = sig + sigf + sigx;
				sigf = FoaDecode.ar(sigf, b2a);
				convsig = [
					FreeVerb2.ar(sig[0], sig[1], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[2], sig[3], mix: 1, room: room, damp: damp)];
				convsig = FoaEncode.ar(convsig.flat, a2b);
				convsig = convsig * env;
				Out.ar(fumabus, convsig);
			}).add;

		} {

			SynthDef("revGlobalAmb_free",  { | gate = 1, room = 0.5, damp = 0.5 |
				var env, w, x, y, z, r, s, t, u, v,
				soaSig, tmpsig, sig, sigx, sigf = In.ar(gbfbus, 9);
				sigx = In.ar(gbixfbus, 9);
				sigx = HOAConvert.ar(2, sigx, \FuMa, \ACN_N3D);
				sig = In.ar(gbus, 1);
				env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
				sig = sig + sigf + sigx;
				sig = AtkMatrixMix.ar(sig, soa_a12_decoder_matrix);
				tmpsig = [
					FreeVerb2.ar(sig[0], sig[1], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[2], sig[3], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[4], sig[5], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[6], sig[7], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[8], sig[9], mix: 1, room: room, damp: damp),
					FreeVerb2.ar(sig[10], sig[11], mix: 1, room: room, damp: damp)];
				tmpsig = tmpsig.flat * env;
				#w, x, y, z, r, s, t, u, v = AtkMatrixMix.ar(tmpsig,
					soa_a12_encoder_matrix);
				soaSig = [w, x, y, z, r, s, t, u, v];
				Out.ar(fumabus, soaSig);
			}).add;

		};

		//run the makeSpatialisers function for each types of local reverbs

		localReverbFunc = { | lrevRef, p, rirWspectrum, locallev, room = 0.5, damp = 0.5 |
			lrevRef.value = FreeVerb.ar(p, mix: 1, room: room, damp: damp, mul: locallev);
		};

		localReverbStereoFunc = { | lrev1Ref, lrev2Ref, p1, p2, rirZspectrum, locallev,
			room = 0.5, damp = 0.5|
			var temp;
			temp = FreeVerb2.ar(p1, p2, mix: 1, room: room, damp: damp, mul: locallev);
			lrev1Ref.value = temp[0];
			lrev2Ref.value = temp[1];
		};

		// localReverbBFormatFunc = { | lrevRef, p, a0ir, a1ir, a2ir, a3ir,
		// 	a4ir, a5ir, a6ir, a7ir, a8ir, a9ir, a10ir, a11ir, locallev,
		// 	room = 0.5, damp = 0.5|
		// 	var temp, sig;
		//
		// 	if (maxorder == 1) {
		// 		sig = FoaDecode.ar(p, b2a);
		// 		temp = [
		// 			FreeVerb2.ar(sig[0], sig[1], mix: 1, room: room, damp: damp),
		// 		FreeVerb2.ar(sig[2], sig[3], mix: 1, room: room, damp: damp)];
		// 		lrevRef.value = FoaEncode.ar(temp.flat, a2b);
		// 	} {
		// 		sig = AtkMatrixMix.ar(p, soa_a12_decoder_matrix);
		// 		temp = [
		// 			FreeVerb2.ar(sig[0], sig[1], mix: 1, room: room, damp: damp),
		// 			FreeVerb2.ar(sig[2], sig[3], mix: 1, room: room, damp: damp),
		// 			FreeVerb2.ar(sig[4], sig[5], mix: 1, room: room, damp: damp),
		// 			FreeVerb2.ar(sig[6], sig[7], mix: 1, room: room, damp: damp),
		// 			FreeVerb2.ar(sig[8], sig[9], mix: 1, room: room, damp: damp),
		// 		FreeVerb2.ar(sig[10], sig[11], mix: 1, room: room, damp: damp)];
		// 		lrevRef.value = AtkMatrixMix.ar(temp.flat,
		// 		soa_a12_encoder_matrix);
		// 	}
		// };

		makeSpatialisers.value(rev_type:"_free");

		// function for no-reverb option

		localReverbFunc = { | lrevRef, p, rirWspectrum, locallev, room, damp|
		};

		localReverbStereoFunc = { | lrev1Ref, lrev2Ref, p1, p2, rirZspectrum,
			locallev, room, damp |
		};

		// localReverbBFormatFunc = { | lrevRef, p, a0ir, a1ir, a2ir, a3ir,
		// 	a4ir, a5ir, a6ir, a7ir, a8ir, a9ir, a10ir, a11ir, locallev,
		// 	room, damp |
		// };

		makeSpatialisers.value(rev_type:"");

		rirList = Array.newClear();

		if (rirBank.notNil) {

			/////////// START loading rirBank /////////////////////

			var rirName, rirW, rirX, rirY, rirZ, bufWXYZ, rirFLU, rirFRD, rirBLD, rirBRU,
			bufAformat, bufAformat_soa_a12, rirA12, bufsize,
			// prepare list of impulse responses for close and distant reverb
			// selection menue

			rirPath = PathName(rirBank),
			rirNum = 0; // initialize

			rirW = [];
			rirX = [];
			rirY = [];
			rirZ = [];
			bufWXYZ = [];

			rirPath.entries.do({ |item, count|

				if (item.extension == "amb") {
					rirNum = rirNum + 1;

					rirName = item.fileNameWithoutExtension;
					rirList = rirList ++ [rirName];

					rirW = rirW ++ [ Buffer.readChannel(server, item.fullPath,
						channels: [0]) ];
					rirX = rirX ++ [ Buffer.readChannel(server, item.fullPath,
						channels: [1]) ];
					rirY = rirY ++ [ Buffer.readChannel(server, item.fullPath,
						channels: [2]) ];
					rirZ = rirZ ++ [ Buffer.readChannel(server, item.fullPath,
						channels: [3]) ];

					bufWXYZ = bufWXYZ ++ [ Buffer.read(server, item.fullPath) ];
				};

			});

			bufAformat = Array.newClear(rirNum);
			bufAformat_soa_a12 = Array.newClear(rirNum);
			rirFLU = Array.newClear(rirNum);
			rirFRD = Array.newClear(rirNum);
			rirBLD = Array.newClear(rirNum);
			rirBRU = Array.newClear(rirNum);
			bufsize = Array.newClear(rirNum);

			rirWspectrum = Array.newClear(rirNum);
			rirXspectrum = Array.newClear(rirNum);
			rirYspectrum = Array.newClear(rirNum);
			rirZspectrum = Array.newClear(rirNum);

			rirFLUspectrum = Array.newClear(rirNum);
			rirFRDspectrum = Array.newClear(rirNum);
			rirBLDspectrum = Array.newClear(rirNum);
			rirBRUspectrum = Array.newClear(rirNum);

			server.sync;

			rirList.do({ |item, count|

				bufsize[count] = PartConv.calcBufSize(fftsize, rirW[count]);

				bufAformat[count] = Buffer.alloc(server, bufWXYZ[count].numFrames,
					bufWXYZ[count].numChannels);
				bufAformat_soa_a12[count] = Buffer.alloc(server,
					bufWXYZ[count].numFrames, 12);
				// for second order conv

				if (File.exists(rirBank ++ "/" ++ item ++ "_Flu.wav").not) {

					("writing " ++ item ++ "_Flu.wav file in" ++ rirBank).postln;

					{BufWr.ar(FoaDecode.ar(PlayBuf.ar(4, bufWXYZ[count],
						loop: 0, doneAction: 2), b2a),
					bufAformat[count], Phasor.ar(0,
						BufRateScale.kr(bufAformat[count]),
						0, BufFrames.kr(bufAformat[count])));
					Out.ar(0, Silent.ar);
					}.play;

					(bufAformat[count].numFrames / server.sampleRate).wait;

					bufAformat[count].write(rirBank ++ "/" ++ item ++ "_Flu.wav",
						headerFormat: "wav", sampleFormat: "int24");

					"done".postln;

				};


				if (File.exists(rirBank ++ "/" ++ item ++ "_SoaA12.wav").not) {

					("writing " ++ item ++ "_SoaA12.wav file in " ++ rirBank).postln;

					{BufWr.ar(AtkMatrixMix.ar(PlayBuf.ar(4, bufWXYZ[count],
						loop: 0, doneAction: 2),
					foa_a12_decoder_matrix),
					bufAformat_soa_a12[count],
					Phasor.ar(0, BufRateScale.kr(bufAformat[count]), 0,
						BufFrames.kr(bufAformat[count])));
					Out.ar(0, Silent.ar);
					}.play;

					(bufAformat[count].numFrames / server.sampleRate).wait;

					bufAformat_soa_a12[count].write(
						rirBank ++ "/" ++ item ++ "_SoaA12.wav",
						headerFormat: "wav", sampleFormat: "int24");

					"done".postln;

				};

			});

			"Loading rir bank".postln;

			rirList.do({ |item, count|

				server.sync;

				bufAformat[count].free;
				bufWXYZ[count].free;

				rirFLU[count] = Buffer.readChannel(server,
					rirBank ++ "/" ++ item ++ "_Flu.wav",
					channels: [0]);
				rirFRD[count] = Buffer.readChannel(server,
					rirBank ++ "/" ++ item ++ "_Flu.wav",
					channels: [1]);
				rirBLD[count] = Buffer.readChannel(server,
					rirBank ++ "/" ++ item ++ "_Flu.wav",
					channels: [2]);
				rirBRU[count] = Buffer.readChannel(server,
					rirBank ++ "/" ++ item ++ "_Flu.wav",
					channels: [3]);

				rirWspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirXspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirYspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirZspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirWspectrum[count].preparePartConv(rirW[count], fftsize);
				rirW[count].free;
				// don't need time domain data anymore, just needed spectral version
				rirXspectrum[count].preparePartConv(rirX[count], fftsize);
				rirX[count].free;
				rirYspectrum[count].preparePartConv(rirY[count], fftsize);
				rirY[count].free;
				rirZspectrum[count].preparePartConv(rirZ[count], fftsize);
				rirZ[count].free;

				rirFLUspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirFRDspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirBLDspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirBRUspectrum[count] = Buffer.alloc(server, bufsize[count], 1);
				rirFLUspectrum[count].preparePartConv(rirFLU[count], fftsize);
				rirFLU[count].free;
				rirFRDspectrum[count].preparePartConv(rirFRD[count], fftsize);
				rirFRD[count].free;
				rirBLDspectrum[count].preparePartConv(rirBLD[count], fftsize);
				rirBLD[count].free;
				rirBRUspectrum[count].preparePartConv(rirBRU[count], fftsize);
				rirBRU[count].free;

				/////////// END loading rirBank /////////////////////
			});

			if (maxorder == 1) {

				SynthDef("revGlobalAmb_conv",  { | gate = 1,
					fluir, frdir, bldir, bruir |
					var env, temp, convsig, sig, sigx, sigf = In.ar(gbfbus, 4);
					sigx = In.ar(gbixfbus, 4);
					sig = In.ar(gbus, 1);
					sigx = FoaEncode.ar(sigx, n2m);
					env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
					sig = sig + sigf + sigx;
					sig = FoaDecode.ar(sig, b2a);
					convsig = [
						PartConv.ar(sig[0], fftsize, fluir),
						PartConv.ar(sig[1], fftsize, frdir),
						PartConv.ar(sig[2], fftsize, bldir),
						PartConv.ar(sig[3], fftsize, bruir)];
					convsig = FoaEncode.ar(convsig, a2b);
					convsig = convsig * env;
					Out.ar(fumabus, convsig);
				}).add;

			} {

				rirA12 = Array.newClear(12);
				rirA12Spectrum = Array2D(rirNum, 12);

				rirList.do({ |item, count|

					12.do { | i |
						rirA12[i] = Buffer.readChannel(server,
							rirBank ++ "/" ++ item ++ "_SoaA12.wav",
							channels: [i]);
						server.sync;
						rirA12Spectrum[count, i] = Buffer.alloc(server,
							bufsize[count], 1);
						server.sync;
						rirA12Spectrum[count, i].preparePartConv(rirA12[i], fftsize);
						server.sync;
						rirA12[i].free;
					};

				});

				SynthDef("revGlobalAmb_conv",  { | gate = 1, a0ir, a1ir, a2ir, a3ir,
					a4ir, a5ir, a6ir, a7ir, a8ir, a9ir, a10ir, a11ir |
					var env, w, x, y, z, r, s, t, u, v,
					soaSig, tmpsig, sig, sigx, sigf = In.ar(gbfbus, 9);
					sigx = In.ar(gbixfbus, 9);
					sigx = HOAConvert.ar(2, sigx, \ACN_N3D, \FuMa);
					sig = In.ar(gbus, 1);
					env = EnvGen.kr(Env.asr(1), gate, doneAction:2);
					sig = sig + sigf + sigx;
					sig = AtkMatrixMix.ar(sig, soa_a12_decoder_matrix);
					tmpsig = [
						PartConv.ar(sig[0], fftsize, a0ir),
						PartConv.ar(sig[1], fftsize, a1ir),
						PartConv.ar(sig[2], fftsize, a2ir),
						PartConv.ar(sig[3], fftsize, a3ir),
						PartConv.ar(sig[4], fftsize, a4ir),
						PartConv.ar(sig[5], fftsize, a5ir),
						PartConv.ar(sig[6], fftsize, a6ir),
						PartConv.ar(sig[7], fftsize, a7ir),
						PartConv.ar(sig[8], fftsize, a8ir),
						PartConv.ar(sig[9], fftsize, a9ir),
						PartConv.ar(sig[10], fftsize, a10ir),
						PartConv.ar(sig[11], fftsize, a11ir)];
					tmpsig = tmpsig * env;
					#w, x, y, z, r, s, t, u, v = AtkMatrixMix.ar(tmpsig,
						soa_a12_encoder_matrix);
					soaSig = [w, x, y, z, r, s, t, u, v];
					Out.ar(fumabus, soaSig);
				}).add;

			};

			//run the makeSpatialisers function for each types of local reverbs

			localReverbFunc = { | lrevRef, p, wir, locallev, room, damp |
				lrevRef.value = PartConv.ar(p, fftsize, wir, locallev);
			};

			localReverbStereoFunc = { | lrev1Ref, lrev2Ref, p1, p2, zir, locallev,
				room, damp |
				var temp1 = p1, temp2 = p2;
				temp1 = PartConv.ar(p1, fftsize, zir, locallev);
				temp2 = PartConv.ar(p2, fftsize, zir, locallev);
				lrev1Ref.value = temp1 * locallev;
				lrev2Ref.value = temp2 * locallev;
			};


			makeSpatialisers.value(rev_type:"_conv");

			// create fonctions to pass rri busses as Synth arguments

			wSpecPar = {|i|
				[\wir, rirWspectrum[i]]
			};

			zSpecPar = {|i|
				[\zir, rirZspectrum[i]]
			};

			wxyzSpecPar = {|i|
				[\wir, rirWspectrum[i],
					\xir, rirXspectrum[i],
					\yir, rirYspectrum[i],
					\zir, rirZspectrum[i]]
			};

			if (maxorder == 1) {

				irSpecPar = { |i|
					[\fluir, rirFLUspectrum[i],
						\frdir, rirFRDspectrum[i],
						\bldir, rirBLDspectrum[i],
						\bruir, rirBRUspectrum[i]]
				};
			} {
				irSpecPar = { |i|
					[\a0ir, rirA12Spectrum[i, 0],
						\a1ir, rirA12Spectrum[i, 1],
						\a2ir, rirA12Spectrum[i, 2],
						\a3ir, rirA12Spectrum[i, 3],
						\a4ir, rirA12Spectrum[i, 4],
						\a5ir, rirA12Spectrum[i, 5],
						\a6ir, rirA12Spectrum[i, 6],
						\a7ir, rirA12Spectrum[i, 7],
						\a8ir, rirA12Spectrum[i, 8],
						\a9ir, rirA12Spectrum[i, 9],
						\a10ir, rirA12Spectrum[i, 10],
						\a11ir, rirA12Spectrum[i, 11]]
				};
			};

		};

		// Lauch GUI
		if(guiflag) {
			this.gui;
		};

		//////// END SYNTHDEFS ///////////////


		updateSynthInArgs = { | source |
			{
				server.sync;
				this.setSynths(source, \angle, angle[source]);
				this.setSynths(source, \level, level[source]);
				this.setSynths(source, \dopamnt, dplev[source]);
				this.setSynths(source, \glev, glev[source]);
				this.setSynths(source, \llev, llev[source]);
				this.setSynths(source, \rm, rm[source]);
				this.setSynths(source, \dm, dm[source]);
				this.setSynths(source, \azim, spheval[source].theta);
				this.setSynths(source, \elev, spheval[source].phi);
				this.setSynths(source, \radius, spheval[source].rho);

				//	this.setSynths(source, \sp, sp[source]);
				//	this.setSynths(source, \df, df[source]);

				this.setSynths(source, \rotAngle, rlev[source]);
				this.setSynths(source, \directang, dlev[source]);
				this.setSynths(source, \contr, clev[source]);
				this.setSynths(source, \grainrate, grainrate[source]);
				this.setSynths(source, \winsize, winsize[source]);
				this.setSynths(source, \winrand, winrand[source]);

				this.setSynths(source, \aux1, aux1[source]);
				this.setSynths(source, \aux2, aux2[source]);
				this.setSynths(source, \aux3, aux3[source]);
				this.setSynths(source, \aux4, aux4[source]);
				this.setSynths(source, \aux5, aux5[source]);

				this.setSynths(source, \a1check, a1but[source]);
				this.setSynths(source, \a2check, a2but[source]);
				this.setSynths(source, \a3check, a3but[source]);
				this.setSynths(source, \a4check, a4but[source]);
				this.setSynths(source, \a5check, a5but[source]);
			}.fork;
		};

		atualizarvariaveis = {

			nfontes.do { | i |
				//	updateSynthInArgs.value(i);

				if(espacializador[i] != nil) {
					espacializador[i].set(
						//	\mx, num.value  ???
						\angle, angle[i],
						\level, level[i], // ? or in player?
						\dopamnt, dplev[i],
						\glev, glev[i],
						\llev, llev[i],
						\rm, rm[i],
						\dm, dm[i],
						//\mx, xbox[i].value,
						//\my, ybox[i].value,
						//\mz, zbox[i].value,
						\azim, spheval[i].theta,
						\elev, spheval[i].phi,
						\radius, spheval[i].rho,
						//\xoffset, xoffset[i],
						//\yoffset, yoffset[i],
						\sp, sp[i],
						\df, df[i],
						\grainrate, grainrate[i];
						\winsize, winsize[i];
						\winrand, winrand[i];
					);
				};

				if(synt[i] != nil) {

					synt[i].set(
						\level, level[i],
						\rotAngle, rlev[i],
						\directang, dlev[i],
						\contr, clev[i],
						\dopamnt, dplev[i],
						\glev, glev[i],
						\llev, llev[i],
						\rm, rm[i],
						\dm, dm[i],
						//\mx, xbox[i].value,
						//\my, ybox[i].value,

						// ERROR HERE?
						//						\mz, zbox[i].value,
						\azim, spheval[i].theta,
						\elev, spheval[i].phi,
						\radius, spheval[i].rho,
						//\xoffset, xoffset[i],
						//\yoffset, yoffset[i],
						//\mz, zval[i].value,
						\sp, sp[i],
						\df, df[i],
						\grainrate, grainrate[i];
						\winsize, winsize[i];
						\winrand, winrand[i];
					);
				};
			};
		};

		//source only version (perhaps phase put other

		updatesourcevariables = {
			| source |
			if(espacializador[source] != nil) {
				espacializador[source].set(
					//	\mx, num.value  ???
					\angle, angle[source],
					\level, level[source], // ? or in player?
					\dopamnt, dplev[source],
					\glev, glev[source],
					\llev, llev[source],
					\rm, rm[source],
					\dm, dm[source],
					\azim, spheval[source].theta,
					\elev, spheval[source].phi,
					\radius, spheval[source].rho,
					\sp, sp[source],
					\df, df[source],
					\grainrate, grainrate[source];
					\winsize, winsize[source];
					\winrand, winrand[source];
				);
			};
			if(synt[source] != nil) {
				synt[source].set(
					\level, level[source],
					\rotAngle, rlev[source],
					\directang, dlev[source],
					\contr, clev[source],
					\dopamnt, dplev[source],
					\glev, glev[source],
					\llev, llev[source],
					\rm, rm[source],
					\dm, dm[source],
					\azim, spheval[source].theta,
					\elev, spheval[source].phi,
					\radius, spheval[source].rho,
					\sp, sp[source],
					\df, df[source];
					\grainrate, grainrate[source];
					\winsize, winsize[source];
					\winrand, winrand[source];
				);
			};
		};



		// this regulates file playing synths
		watcher = Routine.new({
			"WATCHER!!!".postln;
			inf.do({
				0.1.wait;

				nfontes.do({
					| i |
					{
						//("scn = " ++ scn[i]).postln;
						if ((tfieldProxy[i].value != "") ||
							((scn[i] > 0) && (ncan[i] > 0))
							|| (hwncheckProxy[i].value && (ncan[i] > 0)) ) {
							//var source = Point.new;
							// should use cartesian but it's giving problems
							//source.set(xval[i] + xoffset[i],
							//yval[i] + yoffset[i]);
							//source.set(cartval[i].x, cartval[i].y);
							//("audit = " ++ audit[i]).postln;
							//("distance " ++ i ++ " = " ++ source.rho).postln;
							if (spheval[i].rho > plim) {
								firstTime[i] = true;
								if(espacializador[i].isPlaying) {
									//synthRegistry[i].free;
									runStop.value(i); // to kill SC input synths
									espacializador[i].free; // just in case...
								};
							} {
								if(espacializador[i].isPlaying.not && (isPlay || audit[i])
									&& (firstTime[i]
										|| (tfieldProxy[i].value == ""))) {
									//this.triggerFunc[i].value; // play SC input synth
									firstTime[i] = false;
									runTrigger.value(i);

									if(lp[i] == 0) {

										//tocar.value(i, 1, force: true);
										this.newtocar(i, 0, force: true);
									} {
										// could remake this a random start point
										//tocar.value(i, 1, force: true);
										this.newtocar(i, 0, force: true);
									};
								};
							};
						};
					}.defer;   // CHECK THIS DEFER
				});

				if(guiflag.not) {
					// when there is no gui, Automation callback does not work,
					// so here we monitor when the transport reaches end

					if (control.now > dur) {
						if (autoloopval) {
							control.seek; // note, onSeek not called
						} {
							this.blindControlStop; // stop everything
						};
					};
				};

				if (isPlay) {
					ossiaseekback = false;
					ossiatransport.v_(control.now);
					ossiaseekback = true;
				};
			});
		});


		watcher.play;

		///////////////

		if (serport.notNil) {
			//troutine = this.trackerRoutine; // start parsing of serial head tracker data
			//	kroutine = this.serialKeepItUp;
			troutine.play;
			kroutine.play;
		};

		/// OSSIA bindings

		this.ossia(allCrtitical);

		//// LAUNCH INITIAL SYNTH

		globDec = Synth(\globDecodeSynth,
			target:glbRevDecGrp,addAction: \addToTail);

	} // end initMosca


	free {

		control.quit;
		if (serport.notNil) {
			trackPort.close;
			//				this.trackerRoutine.stop;
			//				this.serialKeepItUp.stop;
		};

		troutine.stop;
		kroutine.stop;
		watcher.stop;

		fumabus.free;
		n3dbus.free;
		nfontes.do { | x |
			espacializador[x].free;
			mbus[x].free;
			sbus[x].free;
			//      bfbus.[x].free;
			sombuf[x].free;
			streambuf[x].free;
			synt[x].free;
			scInBus[x].free;
			//		kespac[x].stop;
		};
		MIDIIn.removeFuncFrom(\sysex, sysex);
		//MIDIIn.disconnect;
		if(revGlobal.notNil){
			revGlobal.free;
		};

		if(globDec.notNil){
			globDec.free
		};

		gbus.free;
		gbfbus.free;

		rirList.do { |item, count|
			rirWspectrum[count].free;
			rirXspectrum[count].free;
			rirYspectrum[count].free;
			rirZspectrum[count].free;
			rirFRDspectrum[count].free;
			rirBLDspectrum[count].free;
			rirFLUspectrum[count].free;
			rirBRUspectrum[count].free;
			if (maxorder > 1) {
				12.do { | i |
					rirA12Spectrum[count, i].free;
				};
			};
		};

		foaEncoderOmni.free;
		foaEncoderSpread.free;
		foaEncoderDiffuse.free;
		b2a.free;
		a2b.free;

		playEspacGrp.free;
		glbRevDecGrp.free;

	}

}