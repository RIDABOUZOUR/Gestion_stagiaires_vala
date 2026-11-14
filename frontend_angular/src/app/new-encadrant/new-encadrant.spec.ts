import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewEncadrant } from './new-encadrant';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('NewEncadrant', () => {
  let component: NewEncadrant;
  let fixture: ComponentFixture<NewEncadrant>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [NewEncadrant],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(NewEncadrant);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
