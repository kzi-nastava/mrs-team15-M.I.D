import { Component, AfterViewInit, ViewChild, ElementRef, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../services/user.service';

import Chart from 'chart.js/auto';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-passanger-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './passanger-report.html',
  styleUrl: './passanger-report.css',
})
export class PassangerReport {
  @ViewChild('ridesCanvas', { static: false }) ridesCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('distanceCanvas', { static: false }) distanceCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('expenseCanvas', { static: false }) expenseCanvas!: ElementRef<HTMLCanvasElement>;

  startDateStr: string | null = null;
  endDateStr: string | null = null;

  private ridesChart: any = null;
  private distanceChart: any = null;
  private expenseChart: any = null;

  loading = false;
  message: string | null = null;
  requestedStart: number | null = null;
  requestedEnd: number | null = null;
  // summary values shown next to charts
  ridesSum: number | null = null;
  ridesAvg: number | null = null;

  kmSum: number | null = null;
  kmAvg: number | null = null;

  moneySum: number | null = null;
  moneyAvg: number | null = null;

  constructor(private userService: UserService, private cdr: ChangeDetectorRef) {}

  ngAfterViewInit(): void {
    // optionally load default report for last 30 days
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  private destroyCharts() {
    [this.ridesChart, this.distanceChart, this.expenseChart].forEach((c) => {
      try { if (c) c.destroy(); } catch (e) {}
    });
  }

  loadReport() {
    this.message = null;
    this.loading = true;
    const start = this.startDateStr ? new Date(this.startDateStr).getTime() : null;
    const end = this.endDateStr ? new Date(this.endDateStr).getTime() : null;
    this.requestedStart = start;
    this.requestedEnd = end;

    try {
      this.userService.getReport(start, end).pipe(
        finalize(() => { this.loading = false; this.cdr.detectChanges(); })
      ).subscribe({
        next: (resp: any) => {
          
          this.message = null;
          this.renderChartsFromResponse(resp);
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          console.error('Failed to load report', err);
          this.message = err?.error?.message || 'Failed to load report.';
          this.cdr.detectChanges();
        }
      });
    } catch (e) {
      console.error('Synchronous error calling getReport', e);
      this.loading = false;
      this.message = 'Failed to request report.';
    }
  }

  private renderChartsFromResponse(resp: any) {
    this.destroyCharts();
    // Backend returns objects keyed by date: ridesPerDay, kmPerDay, moneyPerDay
    const ridesPerDay = resp?.ridesPerDay || resp?.ridesPerDay || null;
    const kmPerDay = resp?.kmPerDay || resp?.distancePerDay || resp?.dailyDistance || null;
    const moneyPerDay = resp?.moneyPerDay || resp?.spentPerDay || resp?.dailySpent || null;

    // Build date labels. Prefer explicit startDate/endDate from backend if provided
    let labels: string[] = [];

    // gather keys from returned per-day objects (may be sparse)
    const addKeys = (obj: any) => {
      if (obj && typeof obj === 'object') {
        Object.keys(obj).forEach(k => { if (k) labels.push(k); });
      }
    };
    addKeys(ridesPerDay);
    addKeys(kmPerDay);
    addKeys(moneyPerDay);
    labels = Array.from(new Set(labels));

    // Determine backend-provided start/end times (if present)
    let backendStartTime: number | null = null;
    let backendEndTime: number | null = null;
    if (resp?.startDate && resp?.endDate) {
      try {
        const s = new Date(resp.startDate as string).getTime();
        const e = new Date(resp.endDate as string).getTime();
        if (!isNaN(s) && !isNaN(e)) { backendStartTime = s; backendEndTime = e; }
      } catch (e) { /* ignore parse errors and fall back */ }
    }

    // If backend did not supply explicit start/end, but we have keys, compute from keys
    if ((backendStartTime == null || backendEndTime == null) && labels.length > 0) {
      const dates = labels.map(s => new Date(s));
      backendStartTime = Math.min(...dates.map(d => d.getTime()));
      backendEndTime = Math.max(...dates.map(d => d.getTime()));
    }

    // If we still don't have a backend range, there is no data to show
    if (backendStartTime == null || backendEndTime == null) {
      labels = [];
    } else {
      // Clamp user-requested range to backend range
      const minTime = this.requestedStart != null ? Math.max(backendStartTime, this.requestedStart) : backendStartTime;
      const maxTime = this.requestedEnd != null ? Math.min(backendEndTime, this.requestedEnd) : backendEndTime;

      if (minTime > maxTime) {
        labels = [];
      } else {
        const fmt = (d: Date) => d.toISOString().slice(0, 10);
        const range: string[] = [];
        for (let t = minTime; t <= maxTime; t += 24 * 60 * 60 * 1000) {
          range.push(fmt(new Date(t)));
        }
        labels = range;
      }
    }

    if (!labels || labels.length === 0) {
      // set numeric summaries to 0 so UI shows zeros for empty selection
      this.ridesSum = 0; this.ridesAvg = 0;
      this.kmSum = 0; this.kmAvg = 0;
      this.moneySum = 0; this.moneyAvg = 0;
      this.message = 'No report data for the selected range.';
      return;
    }

    const rides: number[] = labels.map(l => (ridesPerDay && ridesPerDay[l] != null) ? Number(ridesPerDay[l]) : 0);
    const distances: number[] = labels.map(l => (kmPerDay && kmPerDay[l] != null) ? Number(kmPerDay[l]) : 0);
    const expenses: number[] = labels.map(l => (moneyPerDay && moneyPerDay[l] != null) ? Number(moneyPerDay[l]) : 0);

    // helper to round numeric values and return number
    const roundNum = (val: number, decimals: number) => Number(val.toFixed(decimals));

    // compute raw sums
    const rawRidesSum = rides.reduce((s, v) => s + v, 0);
    const rawKmSum = distances.reduce((s, v) => s + v, 0);
    const rawMoneySum = expenses.reduce((s, v) => s + v, 0);

    // compute summaries: prefer explicit backend fields when available
    this.ridesSum = typeof resp?.sumRides === 'number' ? resp.sumRides : roundNum(rawRidesSum, 0);
    this.ridesAvg = typeof resp?.avgRides === 'number' ? resp.avgRides : (labels.length ? roundNum(this.ridesSum! / labels.length, 2) : 0);

    this.kmSum = typeof resp?.sumKM === 'number' ? resp.sumKM : roundNum(rawKmSum, 3);
    this.kmAvg = typeof resp?.avgKM === 'number' ? resp.avgKM : (labels.length ? roundNum(this.kmSum! / labels.length, 3) : 0);

    this.moneySum = typeof resp?.sumMoney === 'number' ? resp.sumMoney : roundNum(rawMoneySum, 2);
    this.moneyAvg = typeof resp?.avgMoney === 'number' ? resp.avgMoney : (labels.length ? roundNum(this.moneySum! / labels.length, 2) : 0);

    // If all values are zero, show friendly message and don't render charts
    const allZero = (arr: number[]) => arr.every(v => v === 0 || v === null || typeof v === 'undefined');
    if (allZero(rides) && allZero(distances) && allZero(expenses)) {
      // hide numeric summaries when no data
      this.ridesSum = null; this.ridesAvg = null;
      this.kmSum = null; this.kmAvg = null;
      this.moneySum = null; this.moneyAvg = null;
      this.message = 'You have no ride history.';
      return;
    }

    const getCanvasContext = (viewChildRef: ElementRef<HTMLCanvasElement> | undefined, id: string) => {
      try {
        const el = viewChildRef?.nativeElement ?? document.getElementById(id) as HTMLCanvasElement | null;
        if (!el) return null;
        // ensure canvas has fixed height so Chart has space
        if (!el.style.height) el.style.height = '320px';
        return el.getContext('2d');
      } catch (e) { return null; }
    };

    const ctxRides = getCanvasContext(this.ridesCanvas, 'ridesCanvasEl');
    const ctxDistance = getCanvasContext(this.distanceCanvas, 'distanceCanvasEl');
    const ctxExpense = getCanvasContext(this.expenseCanvas, 'expenseCanvasEl');

    this.ridesChart = new Chart(ctxRides, {
      type: 'bar',
      data: { labels, datasets: [{ label: 'Rides', data: rides, backgroundColor: '#000', borderColor: '#000', borderWidth: 1 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { x: { ticks: { color: '#000' } }, y: { ticks: { color: '#000' } } } }
    });

    this.distanceChart = new Chart(ctxDistance, {
      type: 'line',
      data: { labels, datasets: [{ label: 'Distance (km)', data: distances, borderColor: '#000', backgroundColor: '#000', fill: false, borderWidth: 1 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { x: { ticks: { color: '#000' } }, y: { ticks: { color: '#000' } } } }
    });

    this.expenseChart = new Chart(ctxExpense, {
      type: 'line',
      data: { labels, datasets: [{ label: 'Spent / Earned', data: expenses, borderColor: '#000', backgroundColor: '#000', fill: false, borderWidth: 1 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { x: { ticks: { color: '#000' } }, y: { ticks: { color: '#000' } } } }
    });
  }
}
