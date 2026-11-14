import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Stagiaires } from './stagiaires';

describe('Stagiaires', () => {
  let component: Stagiaires;
  let fixture: ComponentFixture<Stagiaires>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Stagiaires]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Stagiaires);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
