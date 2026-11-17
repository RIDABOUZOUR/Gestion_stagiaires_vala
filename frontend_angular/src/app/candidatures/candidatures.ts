import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Candidature, CandidatureStatus } from '../models/candidature.model';
import { DepartementModel } from '../models/stage.model';
import { CandidatureService } from '../services/candidature';
import { Departement } from '../services/departement';
import { ExportService } from '../services/export';
import { extractApiError } from '../shared/api-error';
import { CANDIDATURE_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';
import { enregistrerFichier, messageErreurBlob } from '../shared/telechargement';

@Component({
  selector: 'app-candidatures',
  standalone: false,
  templateUrl: './candidatures.html',
})
export class Candidatures implements OnInit {
  readonly statuts = CANDIDATURE_STATUT;
  readonly listeStatuts = Object.keys(CANDIDATURE_STATUT) as CandidatureStatus[];
  readonly taillePage = 20;

  candidatures: Candidature[] = [];
  departements: DepartementModel[] = [];
  filtres!: FormGroup;
  page = 0;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  export = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly candidatureService: CandidatureService,
    private readonly departementService: Departement,
    private readonly exportService: ExportService,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.filtres = this.fb.group({ keyword: [''], statut: [''], departementId: [''] });
    this.departementService.getDepartements().subscribe({ next: (d) => (this.departements = d) });
    this.charger();
  }

  trackById(_i: number, c: Candidature): number {
    return c.id;
  }

  rechercher(): void {
    this.page = 0;
    this.charger();
  }

  allerPage(page: number): void {
    this.page = page;
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.candidatureService
      .rechercher(this.filtres.value, this.page, this.taillePage)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (p) => {
          this.candidatures = p.content;
          this.totalPages = p.totalPages;
          this.totalElements = p.totalElements;
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  exporter(): void {
    this.export = true;
    this.exportService
      .csv('candidatures', this.filtres.value)
      .pipe(finalize(() => (this.export = false)))
      .subscribe({
        next: (r) => enregistrerFichier(r, 'candidatures.csv'),
        error: async (err) => this.notifications.error(await messageErreurBlob(err, "L'export a échoué.")),
      });
  }
}
