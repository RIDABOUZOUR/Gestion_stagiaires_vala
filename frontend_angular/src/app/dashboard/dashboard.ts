import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  QueryList,
  ViewChildren,
  inject,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import Chart from 'chart.js/auto';
import { finalize, forkJoin } from 'rxjs';
import { environment } from '../../environments/environment';
import { NotificationService } from '../shared/notification/notification.service';

interface ChartConfig {
  id: string;
  title: string;
  color: string;
  icon: string;
  iconBg: string;
  endpoint: string;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: false,
})
export class Dashboard implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly http = inject(HttpClient);
  private readonly notifications = inject(NotificationService);

  loading = true;
  private instances: Chart[] = [];

  @ViewChildren('chartCanvas')
  private canvases!: QueryList<ElementRef<HTMLCanvasElement>>;

  readonly charts: ChartConfig[] = [
    {
      id: 'stagiairesChart',
      title: 'Stagiaires par département',
      color: '#4f46e5',
      icon: 'building-office',
      iconBg: 'bg-indigo-50 text-indigo-600',
      endpoint: 'departements',
    },
    {
      id: 'filiereChart',
      title: 'Stagiaires par filière',
      color: '#0ea5e9',
      icon: 'chart-bar',
      iconBg: 'bg-sky-50 text-sky-600',
      endpoint: 'filieres',
    },
    {
      id: 'organismeChart',
      title: 'Stagiaires par organisme',
      color: '#10b981',
      icon: 'briefcase',
      iconBg: 'bg-emerald-50 text-emerald-600',
      endpoint: 'organismes',
    },
    {
      id: 'encadrantChart',
      title: 'Stagiaires par encadrant',
      color: '#f59e0b',
      icon: 'identification',
      iconBg: 'bg-amber-50 text-amber-600',
      endpoint: 'encadrants',
    },
  ];

  ngOnInit(): void {
    this.loadStatistics();
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  private loadStatistics(): void {
    this.loading = true;
    forkJoin(
      this.charts.map((config) =>
        this.http.get<Record<string, number>>(
          `${environment.apiUrl}/statistiques/${config.endpoint}`,
        ),
      ),
    )
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (results) => this.renderCharts(results),
        error: () => this.notifications.error('Impossible de charger les statistiques.'),
      });
  }

  private renderCharts(results: Record<string, number>[]): void {
    // Chart.js manipule le DOM : on ne l'exécute que dans le navigateur (pas au SSR).
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.destroyCharts();

    const elements = this.canvases.toArray();
    this.instances = this.charts.map((config, index) => {
      const canvas = elements[index]?.nativeElement;
      const data = results[index] ?? {};
      const labels = Object.keys(data);
      const values = Object.values(data);

      return new Chart(canvas, {
        type: 'bar',
        data: {
          labels,
          datasets: [
            {
              label: config.title,
              data: values,
              backgroundColor: config.color,
              borderRadius: 6,
              maxBarThickness: 48,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: { padding: 10, cornerRadius: 8 },
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { precision: 0 },
              grid: { color: '#e2e8f0' },
            },
            x: {
              grid: { display: false },
            },
          },
        },
      });
    });
  }

  private destroyCharts(): void {
    this.instances.forEach((chart) => chart.destroy());
    this.instances = [];
  }
}
