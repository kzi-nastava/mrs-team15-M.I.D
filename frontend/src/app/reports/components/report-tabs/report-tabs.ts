import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-report-tabs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './report-tabs.html',
  styleUrl: './report-tabs.css'
})
export class ReportTabs {
  @Input() ridesSum: number | null = null;
  @Input() ridesAvg: number | null = null;

  @Input() kmSum: number | null = null;
  @Input() kmAvg: number | null = null;

  @Input() moneySum: number | null = null;
  @Input() moneyAvg: number | null = null;
  @Input() isDriver: boolean = false;
  @Input() isAdmin: boolean = false;
}
