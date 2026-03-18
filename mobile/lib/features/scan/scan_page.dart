import 'package:flutter/material.dart';

import '../../core/widgets/placeholder_scaffold.dart';

class ScanPage extends StatelessWidget {
  const ScanPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderScaffold(
      title: 'Scan',
      description: 'QR scan, GPS validation va xu ly ket qua nhan stamp.',
    );
  }
}

