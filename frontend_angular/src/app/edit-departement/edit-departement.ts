import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Departement } from '../services/departement';
import { DepartementModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

@Component({
  selector: 'app-edit-departement',
  standalone: false,
  templateUrl: './edit-departement.html',
})
export class EditDepartement implements OnInit {
  form!: FormGroup;
  departementId!: number;
  loading = false;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly departementService: Departement,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.departementId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      responsable: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
    });

    this.loading = true;
    this.departementService
      .getDepartementById(this.departementId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => this.form.patchValue(data),
        error: (err) => {
          this.notifications.error(extractApiError(err));
          this.router.navigate(['/departements']);
        },
      });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  onUpdate(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Veuillez corriger les champs en surbrillance.');
      return;
    }

    const payload: DepartementModel = { id: this.departementId, ...this.form.value };

    this.saving = true;
    this.departementService
      .editDepartement(this.departementId, payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success('Le département a été mis à jour avec succès.');
          this.router.navigate(['/departements']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
