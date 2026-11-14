import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Encadrant } from '../services/encadrant';
import { EncadrantModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

@Component({
  selector: 'app-edit-encadrant',
  standalone: false,
  templateUrl: './edit-encadrant.html',
})
export class EditEncadrant implements OnInit {
  form!: FormGroup;
  encadrantId!: number;
  loading = false;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly encadrantService: Encadrant,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.encadrantId = Number(this.route.snapshot.paramMap.get('id'));

    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      prenom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.pattern(PHONE_PATTERN)]],
    });

    this.loading = true;
    this.encadrantService
      .getEncadrantById(this.encadrantId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => this.form.patchValue(data),
        error: (err) => {
          this.notifications.error(extractApiError(err));
          this.router.navigate(['/encadrants']);
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

    const payload: EncadrantModel = { id: this.encadrantId, ...this.form.value };

    this.saving = true;
    this.encadrantService
      .editEncadrant(this.encadrantId, payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success("L'encadrant a été mis à jour avec succès.");
          this.router.navigate(['/encadrants']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
