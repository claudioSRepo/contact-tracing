import 'package:covd19/ui/colors.dart';
import 'package:covd19/ui/sceensize.dart';
import 'package:flutter/material.dart';

class HomePage extends StatefulWidget {
  @override
  _HomePage createState() => _HomePage();
}

class _HomePage extends State<HomePage> with SingleTickerProviderStateMixin {
  Animation virusBounce;
  Animation shadowFade;
  AnimationController animationController;

  @override
  void initState() {
    super.initState();

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
                    image: AssetImage("assests/allOk.png"),
                    height: screenAwareSize(190, context),
                    width: screenAwareSize(400, context),
                  ),
                ),
                Padding(
                  padding: EdgeInsets.only(top: 50),
                ),
                Center(
                  child: Column(
                    children: [
                      Text(
                        'Il tuo livello di rischio è',
                        style: TextStyle(
                          color: Colors.blueGrey,
                          fontSize: 30,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      SizedBox(height: 30),
                      Text(
                        'BUONO',
                        style: TextStyle(
                          color: Colors.lightGreen,
                          fontSize: 50,
                          fontWeight: FontWeight.bold,
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
}
