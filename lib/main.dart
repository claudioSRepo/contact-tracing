import 'package:covd19/ui/HomePage.dart';
import 'package:covd19/ui/colors.dart';
import 'package:flutter/material.dart';
import 'package:google_nav_bar/google_nav_bar.dart';
import 'package:line_icons/line_icons.dart';

import 'service/service_locator.dart';
import 'ui/Detail.dart';

void main() {
  setupLocator();

  runApp(MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primaryColor: Bgcolor, fontFamily: 'Staatliches'),
      home: StartScreen()));
}

class StartScreen extends StatefulWidget {
  @override
  _StartScreenState createState() => _StartScreenState();
}

class _StartScreenState extends State<StartScreen> {
  int _selectedIndex = 0;

  static const TextStyle optionStyle =
  TextStyle(fontSize: 30, fontWeight: FontWeight.bold);

  static List<Widget> _widgetOptions = <Widget>[
    HomePage(),
    DetailPage(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: Container(
        decoration: BoxDecoration(color: Bgcolor, boxShadow: [
          BoxShadow(blurRadius: 20, color: Colors.white.withOpacity(.1))
        ]),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 50.0, vertical: 8),
            child: GNav(
                gap: 5,
                activeColor: Colors.white,
                color: Colors.white,
                iconSize: 30,
                padding: EdgeInsets.symmetric(horizontal: 40, vertical: 6),
                duration: Duration(milliseconds: 400),
                tabBackgroundColor: Colors.grey[800],
                tabActiveBorder: Border.all(color: Colors.black, width: 1),
                tabBorder: Border.all(color: Colors.grey, width: 1),
                curve: Curves.decelerate,
                tabs: [
                  GButton(
                    icon: LineIcons.home,
                    text: 'Home',
                  ),
                  GButton(
                    icon: LineIcons.bar_chart,
                    text: 'Detail',
                  ),
                ],
                selectedIndex: _selectedIndex,

                onTabChange: (index) {
                  setState(() {
                    _selectedIndex = index;
                  });
                }),
          ),
        ),
      ),
    );
  }
}
