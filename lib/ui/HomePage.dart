import 'package:covd19/ui/colors.dart';
import 'package:covd19/ui/sceensize.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePage createState() => _HomePage();
}

class _HomePage extends State<HomePage> with SingleTickerProviderStateMixin {

  static const platform = const MethodChannel('it.cs.contact.tracing/getRisk');

  Animation virusBounce;
  Animation shadowFade;
  AnimationController animationController;

  String imageRisk;
  String riskZone = "LOW";
  var textRisk = List.filled(3, "", growable: true);
  MaterialColor colorRisk;

  @override
  void initState() {
    super.initState();

    _loadRiskSummary();

    setHomepage();

    animationController =
    AnimationController(vsync: this, duration: Duration(milliseconds: 500))
      ..addListener(() {
        setState(() {});
      })
      ..addStatusListener((status) {
        if (status == AnimationStatus.completed) {
          animationController.reverse();
        } else if (status == AnimationStatus.dismissed) {
          animationController.forward(from: 0.0);
        }
      });
    virusBounce = Tween(begin: Offset(0, 0), end: Offset(0, -5.0))
        .animate(animationController);
    shadowFade = Tween(begin: 10.0, end: 0.0).animate(CurvedAnimation(
        curve: Interval(0.4, 10.0), parent: animationController));

    animationController.forward();
  }

  void setHomepage() {
    switch (riskZone) {
      case "LOW":
        imageRisk = "low.png";
        textRisk[0] = "RISCHIO BASSO";
        textRisk[1] = "Bravo, continua a tenere un comportamento adeguato!";
        textRisk[2] = "";
        colorRisk = Colors.lightGreen;
        break;

      case "MEDIUM":
        imageRisk = "med.png";
        textRisk[0] = "RISCHIO MEDIO";
        textRisk[1] = "Potrebbero esserci dei contatti a rischio.";
        textRisk[2] = "Mantieni due metri di distanza!";
        colorRisk = Colors.amber;
        break;

      case "HIGH":
        imageRisk = "high.png";
        colorRisk = Colors.red;
        textRisk[0] = "RISCHIO ALTO";
        textRisk[1] = "Alcuni contatti sono risultati positivi!";
        textRisk[2] =
        "Non lasciare la tua abitazione, la richiesta di tampone è stata inviata";
        break;

      case "POSITIVE":
        imageRisk = "mask.png";
        colorRisk = Colors.red;
        textRisk[0] = "POSITIVO";
        textRisk[1] = "Purtoppo il tuo tampone è positivo.";
        textRisk[2] = "Per assistenza, chiama il tuo medico.";
        break;

      case "IMMUNE":
        imageRisk = "sneeze.png";
        colorRisk = Colors.lightGreen;
        textRisk[0] = "SEI IMMUNE!";
        textRisk[1] = "Sei stato vaccinato o hai già passato il covid.";
        textRisk[2] = "Tieni comunque un comportamento adeguato";
        break;
    }
  }

  @override
  void dispose() {
    animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      backgroundColor: Bgcolor,
      body: SingleChildScrollView(
        child: Container(
          padding: EdgeInsets.fromLTRB(25, 50, 25, 25),
          child: Center(
            child: Column(
              children: <Widget>[
                Transform.translate(
                  offset: virusBounce.value,
                  child: Image(
                    alignment: Alignment.center,
                    image: AssetImage("assests/" + imageRisk),
                    height: screenAwareSize(190, context),
                    width: screenAwareSize(400, context),
                  ),
                ),
                Padding(
                  padding: EdgeInsets.only(top: 40),
                ),
                Center(
                  child: Column(
                    children: [
                      Text(
                        'La tua situazione è:',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.blueGrey,
                          fontSize: 30,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 20),
                      Text(
                        textRisk[0],
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: colorRisk,
                          fontSize: 60,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 15),
                      Text(
                        textRisk[1],
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: colorRisk,
                          fontSize: 20,
                          fontWeight: FontWeight.normal,
                        ),
                      ),
                      Text(
                        textRisk[2],
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: colorRisk,
                          fontSize: 20,
                          fontWeight: FontWeight.normal,
                        ),
                      )
                    ],
                  ),
                ),
                SizedBox(height: 20)
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<String> _loadRiskSummary() async {
    final String risk = await platform.invokeMethod('getSummaryRisk');

    setState(() {
      riskZone = risk;
      setHomepage();
    });
  }
}
