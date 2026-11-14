import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditStagiaire } from './edit-stagiaire';

describe('EditStagiaire', () => {
  let component: EditStagiaire;
  let fixture: ComponentFixture<EditStagiaire>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EditStagiaire]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditStagiaire);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
