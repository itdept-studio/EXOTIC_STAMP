import 'package:flutter/material.dart';

import '../../core/widgets/placeholder_scaffold.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderScaffold(
      title: 'Home',
      description: 'Trang tong quan mobile app voi luong vao scan, station va rewards.',
    );
  }
}

