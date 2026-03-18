import 'package:flutter/material.dart';

import '../../core/widgets/placeholder_scaffold.dart';

class StationPage extends StatelessWidget {
  const StationPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderScaffold(
      title: 'Stations',
      description: 'Danh sach ga, chi tiet ga va entry point cho check-in.',
    );
  }
}

