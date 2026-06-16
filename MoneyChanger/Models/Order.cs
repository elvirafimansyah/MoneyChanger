using System;
using System.Collections.Generic;

namespace MoneyChanger.Models;

public partial class Order
{
    public int Id { get; set; }

    public string Code { get; set; } = null!;

    public int OriginCurrencyId { get; set; }

    public int TargetCurrencyId { get; set; }

    public decimal ConversionRate { get; set; }

    public decimal OriginNominal { get; set; }

    public decimal TargetNominal { get; set; }

    public DateTime OrderDate { get; set; }

    public virtual Currency OriginCurrency { get; set; } = null!;

    public virtual Currency TargetCurrency { get; set; } = null!;
}
