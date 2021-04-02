
import 'package:covd19/service/getdata.dart';
import 'package:covd19/ui/colors.dart';
import 'package:flutter/material.dart';

class DetailPage extends StatefulWidget {
  @override
  _DetailPageState createState() => _DetailPageState();
}

class _DetailPageState extends State<DetailPage> {
  var jsondata;
  var cases;
  var now = new DateTime.now();
  CoronaDetails d;

  // String s = "https://api-corona.herokuapp.com/total";

  Future<void> getData() async {
    // final response = await http.get(s);
    // jsondata = json.decode(response.body);
    d = CoronaDetails.fromJson({
      "Main": [
        {
          "CoronaCases": "10",
          "CoronaClose": "1",
          "CoronaCritical": "2",
          "CoronaCurrent": "3",
          "CoronaDeaths": "5"
        }
      ]
    });

    setState(() {});
  }

  @override
  void initState() {
    super.initState();
    getData();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      backgroundColor: Bgcolor,
      body: d == null
          ? Center(
        child: CircularProgressIndicator(
          backgroundColor: Colors.white,
          valueColor: new AlwaysStoppedAnimation<Color>(Fgcolor),
        ),
      )
          : RefreshIndicator(
        onRefresh: getData,
        child: ListView(
          physics: BouncingScrollPhysics(),
          children: d.main
              .map(
                (pointer) => Padding(
              padding: EdgeInsets.only(
                left: 20,
                top: 40,
              ),
              child: Column(
                children: <Widget>[
                  SizedBox(
                    height: 20,
                  ),
                  Center(
                    child: Text(
                      "COVID-19",
                      style: TextStyle(
                        fontSize: 30,
                        fontWeight: FontWeight.bold,
                        inherit: true,
                        color: Colors.white,
                        letterSpacing: 0.4,
                      ),
                    ),
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Row(
                    children: <Widget>[
                      Text("provaw"),
                    ],
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Row(
                    children: <Widget>[Text("prova")],
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    "Active Cases",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      inherit: true,
                      letterSpacing: 0.4,
                    ),
                  ),
                  SizedBox(
                    height: 10,
                  ),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    "CLOSED CASES",
                    style: TextStyle(
                      fontSize: 24,
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      inherit: true,
                      letterSpacing: 0.4,
                    ),
                  ),
                  SizedBox(
                    height: 10,
                  ),
                ],
              ),
            ),
          )
              .toList(),
        ),
      ),
    );
  }
}
