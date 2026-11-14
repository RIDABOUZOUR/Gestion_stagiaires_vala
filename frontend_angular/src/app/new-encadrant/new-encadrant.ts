import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Encadrant } from '../services/encadrant';
import { EncadrantModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { extractApiError } from '../shared/api-error';

const PHONE_PATTERN = /^(\+\d{1,3}[-. ]?)?\d{10}$/;

@Component({
  selector: 'app-new-encadrant',
  standalone: false,
  templateUrl: './new-encadrant.html',
  styleUrl: './new-encadrant.css',
})
export class NewEncadrant implements OnInit {
  form!: FormGroup;
  saving = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly encadrantService: Encadrant,
    private readonly router: Router,
    private readonly notifications: NotificationService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      prenom: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.pattern(PHONE_PATTERN)]],
    });
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  handleSaveEncadrant(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Veuillez corriger les champs en surbrillance.');
      return;
    }

    const value = this.form.value;
    const payload: EncadrantModel = {
      id: 0,
      nom: value.nom,
      prenom: value.prenom,
      email: value.email,
      telephone: value.telephone,
    };

    this.saving = true;
    this.encadrantService
      .addEncadrant(payload)
      .pipe(finalize(() => (this.saving = false)))
      .subscribe({
        next: () => {
          this.notifications.success("L'encadrant a été enregistré avec succès.");
          this.router.navigate(['/encadrants']);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }
}
