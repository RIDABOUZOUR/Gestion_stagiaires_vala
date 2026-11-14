import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Stagiaire } from '../services/stagiaire';
import { StagiaireModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { ConfirmService } from '../shared/confirm/confirm.service';

@Component({
  selector: 'app-stagiaires',
  standalone: false,
  templateUrl: './stagiaires.html',
  styleUrl: './stagiaires.css',
})
export class Stagiaires implements OnInit {
  stagiaires: StagiaireModel[] = [];
  searchFormGroup!: FormGroup;
  loading = false;

  constructor(
    private readonly stagiaireService: Stagiaire,
    private readonly fb: FormBuilder,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
  ) {}

  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({ keyword: this.fb.control('') });
    this.loadStagiaires();
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  loadStagiaires(): void {
    this.loading = true;
    this.stagiaireService
      .getStagiaires()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stagiaires = data),
        error: () => this.notifications.error('Impossible de charger la liste des stagiaires.'),
      });
  }

  handleSearchStagiaires(): void {
    const keyword = String(this.searchFormGroup.value.keyword ?? '').trim();
    this.loading = true;
    this.stagiaireService
      .searchStagiaires(keyword)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stagiaires = data),
        error: () => this.notifications.error('La recherche a échoué.'),
      });
  }

  async handleDeleteStagiaire(stagiaire: StagiaireModel): Promise<void> {
    const confirmed = await this.confirm.confirm({
      title: 'Supprimer le stagiaire',
      message: `Voulez-vous vraiment supprimer « ${stagiaire.prenom} ${stagiaire.nom} » ?`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.stagiaireService.deleteStagiaire(stagiaire.id).subscribe({
      next: () => {
        this.stagiaires = this.stagiaires.filter((item) => item.id !== stagiaire.id);
        this.notifications.success(
          `Le stagiaire « ${stagiaire.prenom} ${stagiaire.nom} » a été supprimé.`,
        );
      },
      error: () => this.notifications.error('La suppression a échoué.'),
    });
  }
}
