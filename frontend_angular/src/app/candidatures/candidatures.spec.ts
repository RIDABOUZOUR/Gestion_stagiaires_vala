import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { Candidatures } from './candidatures';

describe('Candidatures', () => {
  let fixture: ComponentFixture<Candidatures>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Candidatures],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ id: '1' }), queryParamMap: convertToParamMap({}) } } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(Candidatures);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
