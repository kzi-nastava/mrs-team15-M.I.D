import { Component, AfterViewInit, ViewChild, ElementRef, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';
import { ReportTabs } from '../../components/report-tabs/report-tabs';

import Chart from 'chart.js/auto';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-admin-report',
  standalone: true,
  imports: [CommonModule, FormsModule, ReportTabs],
  templateUrl: './admin-report.html',
  styleUrls: ['./admin-report.css']
})
export class AdminReport implements AfterViewInit, OnDestroy {
  @ViewChild('ridesCanvas', { static: false }) ridesCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('distanceCanvas', { static: false }) distanceCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('expenseCanvas', { static: false }) expenseCanvas!: ElementRef<HTMLCanvasElement>;

  startDateStr: string | null = null;
  endDateStr: string | null = null;

  // scope can be 'ALL_DRIVERS' | 'ALL_USERS' | 'SINGLE_USER'
  scope: string = 'ALL_DRIVERS';
  selectedUserId: string | null = null;
  selectedUserDisplay: string | null = null;

  // user search
  userSearchTerm: string = '';
  userSearchResults: any[] = [];
  userSearchLoading = false;
  private userSearchTimeout: any = null;

  private ridesChart: any = null;
  private distanceChart: any = null;
  private expenseChart: any = null;

  loading = false;
  message: string | null = null;
  requestedStart: number | null = null;
  requestedEnd: number | null = null;
  ridesSum: number | null = null;
  ridesAvg: number | null = null;
  kmSum: number | null = null;
  kmAvg: number | null = null;
  moneySum: number | null = null;
  moneyAvg: number | null = null;

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngAfterViewInit(): void {}

  ngOnDestroy(): void { this.destroyCharts(); }

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

    const params: any = { startDate: start, endDate: end };
    // map scope to drivers/users/personId query params expected by backend
    if (this.scope === 'ALL_DRIVERS') {
      params.drivers = true;
      params.users = false;
    } else if (this.scope === 'ALL_USERS') {
      params.drivers = false;
      params.users = true;
    } else if (this.scope === 'SINGLE_USER') {
      params.drivers = false;
      params.users = true;
      if (this.selectedUserId) params.personId = this.selectedUserId;
    }

    try {
      this.adminService.getReport(params).pipe(
        finalize(() => { this.loading = false; this.cdr.detectChanges(); })
      ).subscribe({
        next: (resp: any) => {
          this.message = null;
          this.renderChartsFromResponse(resp);
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          console.error('Failed to load admin report', err);
          this.message = err?.error?.message || 'Failed to load report.';
          this.cdr.detectChanges();
        }
      });
    } catch (e) {
      console.error('Synchronous error calling admin getReport', e);
      this.loading = false;
      this.message = 'Failed to request report.';
    }
  }

  onUserInputChange(val: string) {
    this.selectedUserDisplay = val;
    this.userSearchTerm = val;
    this.selectedUserId = null;
    // debounce
    if (this.userSearchTimeout) clearTimeout(this.userSearchTimeout);
    if (!val || val.length < 2) { this.userSearchResults = []; return; }
    this.userSearchTimeout = setTimeout(() => this.searchUsers(val), 300);
  }

  searchUsers(term: string) {
    this.userSearchLoading = true;
    this.userSearchResults = [];
    this.adminService.getAllUsers(term, undefined, undefined, 0, 10).subscribe({
      next: (resp: any) => {
        // backend returns Page<UserResponseDTO>
        const results = resp?.content ?? resp;
        this.userSearchResults = Array.isArray(results) ? results : [];
        this.userSearchLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('User search failed', err);
        this.userSearchLoading = false;
        this.userSearchResults = [];
        this.cdr.detectChanges();
      }
    });
  }

  selectUser(user: any) {
    this.selectedUserId = user.id ?? user.userId ?? user.personId ?? user.email ?? null;
    this.selectedUserDisplay = user.fullName || user.name || user.email || String(this.selectedUserId);
    this.userSearchResults = [];
  }

  private renderChartsFromResponse(resp: any) {
    this.destroyCharts();
    const ridesPerDay = resp?.ridesPerDay || resp?.ridesPerDay || null;
    const kmPerDay = resp?.kmPerDay || resp?.distancePerDay || resp?.dailyDistance || null;
    const moneyPerDay = resp?.moneyPerDay || resp?.spentPerDay || resp?.dailySpent || null;

    let labels: string[] = [];
    const addKeys = (obj: any) => {
      if (obj && typeof obj === 'object') {
        Object.keys(obj).forEach(k => { if (k) labels.push(k); });
      }
    };
    addKeys(ridesPerDay);
    addKeys(kmPerDay);
    addKeys(moneyPerDay);
    labels = Array.from(new Set(labels));

    let backendStartTime: number | null = null;
    let backendEndTime: number | null = null;
    if (resp?.startDate && resp?.endDate) {
      try {
        const s = new Date(resp.startDate as string).getTime();
        const e = new Date(resp.endDate as string).getTime();
        if (!isNaN(s) && !isNaN(e)) { backendStartTime = s; backendEndTime = e; }
      } catch (e) { }
    }

    if ((backendStartTime == null || backendEndTime == null) && labels.length > 0) {
      const dates = labels.map(s => new Date(s));
      backendStartTime = Math.min(...dates.map(d => d.getTime()));
      backendEndTime = Math.max(...dates.map(d => d.getTime()));
    }

    if (backendStartTime == null || backendEndTime == null) {
      labels = [];
    } else {
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
      this.ridesSum = 0; this.ridesAvg = 0;
      this.kmSum = 0; this.kmAvg = 0;
      this.moneySum = 0; this.moneyAvg = 0;
      this.message = 'No report data for the selected range.';
      return;
    }

    const rides: number[] = labels.map(l => (ridesPerDay && ridesPerDay[l] != null) ? Number(ridesPerDay[l]) : 0);
    const distances: number[] = labels.map(l => (kmPerDay && kmPerDay[l] != null) ? Number(kmPerDay[l]) : 0);
    const expenses: number[] = labels.map(l => (moneyPerDay && moneyPerDay[l] != null) ? Number(moneyPerDay[l]) : 0);

    const roundNum = (val: number, decimals: number) => Number(val.toFixed(decimals));

    const rawRidesSum = rides.reduce((s, v) => s + v, 0);
    const rawKmSum = distances.reduce((s, v) => s + v, 0);
    const rawMoneySum = expenses.reduce((s, v) => s + v, 0);

    this.ridesSum = typeof resp?.sumRides === 'number' ? resp.sumRides : roundNum(rawRidesSum, 0);
    this.ridesAvg = typeof resp?.avgRides === 'number' ? resp.avgRides : (labels.length ? roundNum(this.ridesSum! / labels.length, 2) : 0);

    this.kmSum = typeof resp?.sumKM === 'number' ? resp.sumKM : roundNum(rawKmSum, 3);
    this.kmAvg = typeof resp?.avgKM === 'number' ? resp.avgKM : (labels.length ? roundNum(this.kmSum! / labels.length, 3) : 0);

    this.moneySum = typeof resp?.sumMoney === 'number' ? resp.sumMoney : roundNum(rawMoneySum, 2);
    this.moneyAvg = typeof resp?.avgMoney === 'number' ? resp.avgMoney : (labels.length ? roundNum(this.moneySum! / labels.length, 2) : 0);

    const allZero = (arr: number[]) => arr.every(v => v === 0 || v === null || typeof v === 'undefined');
    if (allZero(rides) && allZero(distances) && allZero(expenses)) {
      this.ridesSum = null; this.ridesAvg = null;
      this.kmSum = null; this.kmAvg = null;
      this.moneySum = null; this.moneyAvg = null;
      this.message = 'No data available for the chosen parameters.';
      return;
    }

    const getCanvasContext = (viewChildRef: ElementRef<HTMLCanvasElement> | undefined, id: string) => {
      try {
        const el = viewChildRef?.nativeElement ?? document.getElementById(id) as HTMLCanvasElement | null;
        if (!el) return null;
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
      data: { labels, datasets: [{ label: 'Amount', data: expenses, borderColor: '#000', backgroundColor: '#000', fill: false, borderWidth: 1 }] },
      options: { responsive: true, maintainAspectRatio: false, scales: { x: { ticks: { color: '#000' } }, y: { ticks: { color: '#000' } } } }
    });
  }
}
