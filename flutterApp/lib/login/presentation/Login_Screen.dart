import 'package:flutterapp/login/application/auth.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class LoginScreen extends StatefulWidget {
  const LoginScreen ({super.key});
  @override
  State<StatefulWidget> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  late TextEditingController usernameController;
  late TextEditingController passwordController;

  @override
  void initState() {
    usernameController = TextEditingController();
    passwordController = TextEditingController();

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
          child: TextField(
            controller: usernameController,
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(90.0),
              ),
              labelText: 'Username',
            ),
          ),
        ),
        Container(
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
          child: TextField(
            controller: passwordController,
            decoration: InputDecoration(
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(90.0),
              ),
              labelText: 'Password',
            ),
          ),
        ),
        Container(
          height: 80,
          padding: const EdgeInsets.all(20),
          child: ElevatedButton(
            style: ElevatedButton.styleFrom(
              minimumSize: const Size.fromHeight(50),
            ),
            child: const Text('Log In'),
            onPressed: () =>
                logInButtonPressed(
                    usernameController.text, passwordController.text),
          ),
        ),
        TextButton(
            onPressed: () {},
            style: ButtonStyle(
              foregroundColor: MaterialStateProperty.all<Color>(Colors.blue),
            ),
            child: const Text('Forgot Password?')
        ),
      ],
    );
  }

  void logInButtonPressed(String username, String password) {

  }
}