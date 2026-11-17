import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { StageDetail } from './stage-detail';

describe('StageDetail', () => {
  let fixture: ComponentFixture<StageDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StageDetail],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }), queryParamMap: convertToParamMap({}) } } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(StageDetail);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});

describe('StageDetail — actions de statut', () => {
  it("propose démarrer/annuler pour un stage planifié et rien pour un stage terminé", async () => {
    await TestBed.configureTestingModule({
      declarations: [StageDetail],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }) } } },
      ],
    }).compileComponents();
    const component = TestBed.createComponent(StageDetail).componentInstance;

    component.stage = { status: 'PLANIFIE' } as never;
    expect(component.actions.map((a) => a.action)).toEqual(['demarrer', 'annuler']);
    component.stage = { status: 'EN_COURS' } as never;
    expect(component.actions.map((a) => a.action)).toEqual(['terminer', 'annuler']);
    component.stage = { status: 'TERMINE' } as never;
    expect(component.actions).toEqual([]);
  });
});
