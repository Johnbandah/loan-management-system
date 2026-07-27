exports.calculateEMI = (amount, tenureMonths, annualInterestRate) => {
  const P = amount;
  const n = tenureMonths;
  const r = annualInterestRate / 100 / 12;

  let emi, totalPayable, totalInterest;

  if (r === 0) {
    emi = P / n;
  } else {
    emi = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
  }

  totalPayable = emi * n;
  totalInterest = totalPayable - P;

  return {
    monthlyEMI: Math.round(emi * 100) / 100,
    totalPayable: Math.round(totalPayable * 100) / 100,
    totalInterest: Math.round(totalInterest * 100) / 100,
    principalAmount: P,
    tenureMonths: n
  };
};

exports.calculateAmortizationSchedule = (amount, tenureMonths, annualInterestRate) => {
  const result = this.calculateEMI(amount, tenureMonths, annualInterestRate);
  const emi = result.monthlyEMI;
  const r = annualInterestRate / 100 / 12;

  let schedule = [];
  let balance = amount;

  for (let i = 1; i <= tenureMonths; i++) {
    const interest = balance * r;
    const principal = emi - interest;
    balance -= principal;

    schedule.push({
      installmentNumber: i,
      principal,
      interest,
      emi,
      remainingBalance: Math.max(0, balance)
    });
  }

  return schedule;
};